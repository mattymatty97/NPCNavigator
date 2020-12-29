package com.mattymatty.NPCNavigator.DStarLite;


import com.mattymatty.NPCNavigator.NPCNavigator;
import org.bukkit.World;

import java.util.*;
import java.util.stream.Collectors;

public class DStarLite {

    private static HashMap<World, HashMap<State, CellCacheItem>> worldMap = new HashMap<>();

    private HashMap<State, CellCacheItem> cellCache;

    private World world;

    List<State> path = new LinkedList<>();

    private State s_start = new State();
    private State s_goal = new State();
    private State s_last = new State();

    private int maxCalc = 0;
    private double k_m = 0;

    private PriorityQueue<State> openList = new PriorityQueue<State>();
    private HashSet<State> openHash = new HashSet<>();

    public HashMap<State, CellInfo> cellMap = new HashMap<State, CellInfo>();


    private static final double SQRT3 = Math.sqrt(3.0);
    private static final double SQRT2 = Math.sqrt(2.0);

    public DStarLite(World world) {
        maxCalc = 80000;
        this.world = world;
        cellCache = worldMap.computeIfAbsent(world, k -> new HashMap<>());
    }

    public void init(int sX, int sY, int sZ, int gX, int gY, int gZ) {
        cellMap.clear();
        path.clear();
        openList.clear();
        openHash.clear();

        s_start.x = sX;
        s_start.y = sY;
        s_start.z = sZ;
        s_goal.x = gX;
        s_goal.y = gY;
        s_goal.z = gZ;

        CellInfo tmp = new CellInfo();
        tmp.g = 0;
        tmp.rhs = 0;

        cellMap.put(s_goal, tmp);
        s_last = s_start;

        s_goal.k.setFirst(heuristic(s_start, s_goal));
        s_goal.k.setSecond(0.0);

        openList.add(s_goal);
        openHash.add(s_goal);
    }

    private State calculateKey(State u) {
        double val = Math.min(getRHS(u), getG(u));

        u.k.setFirst(val + heuristic(u, s_start) + k_m);
        u.k.setSecond(val);

        return u;
    }

    /*
     * Returns the rhs value for state u.
     */
    private double getRHS(State u) {
        CellInfo cell = cellMap.get(u);

        if (cell == null)
            return Double.POSITIVE_INFINITY;
        return cell.rhs;
    }

    /*
     * Returns the g value for the state u.
     */
    private double getG(State u) {
        CellInfo cell = cellMap.get(u);

        if (cell == null)
            return Double.POSITIVE_INFINITY;
        return cell.g;
    }

    /*
     * Sets the G value for state u
     */
    private void setG(State u, double g) {
        makeNewCell(u).g = g;
    }

    /*
     * Sets the rhs value for state u
     */
    private void setRHS(State u, double rhs) {
        makeNewCell(u).rhs = rhs;
    }

    /*
     * Checks if a cell is in the hash table, if not it adds it in.
     */
    private CellInfo makeNewCell(State u) {
        CellInfo cell = cellMap.get(u);
        if (cell != null) return cell;
        cell = new CellInfo();
        cell.g = cell.rhs = Double.POSITIVE_INFINITY;
        cellMap.put(u, cell);
        return cell;
    }


    /*
     * Returns the heuristic value between two states
     */
    private double heuristic(State a, State b) {
        //return trueDist3D(a,b);
        return (NPCNavigator.instance.approxCost)?approx3dDistance(a, b):trueDist3D(a,b);
    }

    /*
     * Returns the fast approximated distance between state a and state b
     */

    private double approx3dDistance(State a, State b) {
        int dx = Math.abs(a.x - b.x);
        double dy = Math.abs(a.y - b.y);
        int dz = Math.abs(a.z - b.z);

        return
                0.5 * (1 + 1 / (4 * SQRT3))
                        * Math.min((dx + dy + dz) / SQRT3, Math.max(dx, Math.max(dy, dz)))
                ;

    }

    private double approx2dDistance(State a, State b) {
        int dx = Math.abs(a.x - b.x);
        int dz = Math.abs(a.z - b.z);

        return
                0.5 * (
                        1 + 1 / ((4 - 2 * SQRT2))
                                * Math.min((dx + dz) / SQRT2, Math.max(dx, dz))
                )
                ;
    }


    public boolean replan() {
        path.clear();

        int res = computeShortestPath();
        if (res < 0) {
            System.out.println("No Path to Goal");
            return false;
        }

        LinkedList<State> n;
        State cur = s_start;

        if (getG(s_start) == Double.POSITIVE_INFINITY) {
            System.out.println("No Path to Goal");
            return false;
        }

        while (cur.neq(s_goal)) {
            path.add(cur);
            n = getSucc(cur);

            if (n.isEmpty()) {
                System.out.println("No Path to Goal");
                return false;
            }

            double cmin = Double.POSITIVE_INFINITY;
            double tmin = 0;
            State smin = new State();

            for (State i : n) {
                double val = cost(cur, i, false);
                double val2 = trueDist3D(i, s_goal) + trueDist3D(s_start, i);
                val += getG(i);

                if (close(val, cmin)) {
                    if (tmin > val2) {
                        tmin = val2;
                        cmin = val;
                        smin = i;
                    }
                } else if (val < cmin) {
                    tmin = val2;
                    cmin = val;
                    smin = i;
                }
            }
            n.clear();
            cur = new State(smin);
            //cur = smin;
        }
        path.add(s_goal);
        return true;
    }

    /*
     * As per [S. Koenig,2002]
     */
    private int computeShortestPath() {
        LinkedList<State> s;

        if (openList.isEmpty()) return 1;

        int k = 0;
        while ((!openList.isEmpty()) && (
                (openList.peek().lt(calculateKey(s_start))) ||
                        (getRHS(s_start) != getG(s_start))
        )) {

            if (k++ > maxCalc) {
                System.out.println("At Max Try");
                return -1;
            }

            State u = null;

            while (!openList.isEmpty()) {
                u = openList.poll();
                if (openHash.contains(u))
                    break;
            }

            assert u != null : "list was empty";
            State k_old = new State(u);

            if (k_old.lt(calculateKey(u))) { //u is out of date
                openList.add(u); //update u
            } else if (getG(u) > getRHS(u)) { //needs update (got better)
                setG(u, getRHS(u));
                openHash.remove(u); //remove from open set

                s = getPred(u);
                for (State i : s) {
                    if (i.neq(s_goal)) {
                        setRHS(i, Math.min(getRHS(i), cost(i, u) + getG(u)));
                    }
                    updateVertex(i);
                }
            } else {                         // g <= rhs, state has got worse
                double old_g = getG(u);
                setG(u, Double.POSITIVE_INFINITY);
                s = getPred(u);
                s.add(u);
                for (State i : s) {
                    if (getRHS(i) == cost(i, u) + old_g) {
                        if (i.neq(s_goal)) {
                            setRHS(i, minRHSSuccesssors(i));
                        }
                    }
                    updateVertex(i);
                }
            }
        } //while
        return 0;
    }

    private double minRHSSuccesssors(State u) {
        LinkedList<State> s = getSucc(u);
        double min = Double.POSITIVE_INFINITY;
        double tmp;

        for (State i : s) {
            tmp = getG(i) + cost(u, i);
            if (tmp < min)
                min = tmp;
        }
        return min;
    }

    private void updateVertex(State u) {
        if (openHash.contains(u)) {
            if (getG(u) != getRHS(u)) {
                openHash.remove(u);
                u = new State(u);
                calculateKey(u);
                openList.add(u);
                openHash.add(u);
            } else {
                openList.remove(u);
                openHash.remove(u);
            }
        } else {
            if (getG(u) != getRHS(u)) {
                calculateKey(u);
                openHash.add(u);
                openList.add(u);
            }
        }
    }

    /*
     * Returns a list of all the predecessor states for state u. Since
     * this is for an 8-way connected graph, the list contains all the
     * neighbours for state u. Occupied neighbours are not added to the list
     */
    private LinkedList<State> getPred(State u) {
        LinkedList<State> s = new LinkedList<>();
        State tempState;
        getMovements(u, 0).stream().filter(this::traversable).forEach(s::add);
        //up one floor
        getMovements(u, 1).stream().filter(this::traversable).forEach(s::add);
        //down one floor
        getMovements(u, -1).stream().filter(this::traversable).forEach(s::add);
        return s;
    }

    private LinkedList<State> getMovements(State u, int dy) {
        LinkedList<State> s = new LinkedList<>();
        s.add(new State(u.x + 1, u.y + dy, u.z) ); //EAST
        s.add(new State(u.x + 1, u.y + dy, u.z + 1) ); //SOUTH-EAST
        s.add(new State(u.x , u.y + dy, u.z + 1) ); //SOUTH
        s.add(new State(u.x - 1, u.y + dy, u.z + 1) ); //SOUTH-WEST
        s.add(new State(u.x - 1, u.y + dy, u.z ) ); //WEST
        s.add(new State(u.x - 1, u.y + dy, u.z - 1) ); //NORTH-WEST
        s.add(new State(u.x, u.y + dy, u.z - 1) ); //NORTH
        s.add(new State(u.x + 1, u.y + dy, u.z - 1) ); //NORTH-EAST
        return s;
    }

    /*
     * Returns a list of successor states for state u, since this is an
     * 8-way graph this list contains all of a cells neighbours. Unless
     * the cell is occupied, in which case it has no successors.
     */
    private LinkedList<State> getSucc(State u) {
        LinkedList<State> s = new LinkedList<>();
        State tempState;

        if (!traversable(u)) return s;
        //starting with same floor
        s.addAll(getMovements(u,0));
        //up one floor
        s.addAll(getMovements(u,1));
        //down one floor
        s.addAll(getMovements(u,-1));
        return s;
    }

    /*
     * Returns true if the cell is traversable, false
     * otherwise.
     */
    private boolean traversable(State u) {

        CellCacheItem cell = getCell(u);

        return cell.isPassable();
    }

    private CellCacheItem getCell(State u) {
        CellCacheItem cell = cellCache.get(u);
        //if the cellHash does not contain the State u
        if (cell == null) {
            cell = new CellCacheItem();
            cell.update(u, world);
            cellCache.put(u, cell);
        }
        return cell;
    }

    /*
     * Euclidean cost between state a and state b
     */

    private double trueDist3D(State a, State b) {
        float x = a.x - b.x;
        float y = a.y- b.y;
        float z = a.z - b.z;
        return Math.sqrt(x * x + y * y + z * z);
    }

    private double trueDist2D(State a, State b) {
        float x = a.x - b.x;
        float z = a.z - b.z;
        return Math.sqrt(x * x + z * z);
    }

    /*
     * Returns the cost of moving from state a to state b. This could be
     * either the cost of moving off state a or onto state b, we went with the
     * former.
     */
    private double cost(State a, State b) {
        return cost(a, b, NPCNavigator.instance.approxCost);
    }

    private double cost(State a, State b, boolean approx) {
        CellCacheItem cellA, cellB;
        cellA = getCell(a);
        cellB = getCell(b);

        double cost = (approx) ? approx3dDistance(a, b) : trueDist3D(a, b);

        int dx, dy, dz;
        dx = a.x - b.x;
        dy = a.y - b.y;
        dz = a.z - b.z;

        if (dx != 0 && dz != 0) { //if diagonal
            CellCacheItem cornerB, cornerA;
            cornerB = getCell(new State(b.x, Math.min(a.y, b.y), a.z));
            cornerA = getCell(new State(a.x, Math.min(a.y, b.y), b.z));
            if (dy == 0) { //if walking flat
                if (!cornerA.isPassable() || !cornerB.isPassable()) {
                    cost = Double.POSITIVE_INFINITY;
                    return cost;
                }
            } else { //if changing height
                CellCacheItem cornerB1, cornerA1;
                cornerB1 = getCell(new State(b.x, Math.max(a.y, b.y), a.z));
                cornerA1 = getCell(new State(a.x, Math.max(a.y, b.y), b.z));
                if (!(
                        ((cornerA.isPassable() && (dy > 0) ? cornerA.canJump() : cornerA.canFall()) || cornerA1.isPassable()) &&  //left
                                ((cornerB.isPassable() && (dy > 0) ? cornerB.canJump() : cornerB.canFall()) || cornerB1.isPassable())     //right
                )) {
                    cost = Double.POSITIVE_INFINITY;
                    return cost;
                }
            }

        }

        //if vertical
        if (dy != 0) {
            if (dy > 0 && cellA.canJump()) {
                if (!cellB.isSlab() && ( !cellB.isStair() || !cellB.canClimb(dx, dy, dz) )) {
                    cost = dy + ((approx) ? approx2dDistance(a, b) : trueDist2D(a, b));
                }
            } else if (dy < 0 && cellB.canFall()) {
                if (!cellA.isSlab() && ( !cellA.isStair() || !cellA.canClimb(-dx, dy, -dz) )) {
                    cost = -dy + ((approx) ? approx2dDistance(a, b) : trueDist2D(a, b));
                }
            } else {
                cost = Double.POSITIVE_INFINITY;
                return cost;
            }
        }


        //TODO: additional cost for specific blocks

        return cost;
    }

    /*
     * Returns true if x and y are within 10E-5, false otherwise
     */
    private boolean close(double x, double y) {
        if (x == Double.POSITIVE_INFINITY && y == Double.POSITIVE_INFINITY) return true;
        return (Math.abs(x - y) < 0.00001);
    }

    public List<State> getPath() {
        return Collections.unmodifiableList(path);
    }

    public World getWorld() {
        return world;
    }
}
