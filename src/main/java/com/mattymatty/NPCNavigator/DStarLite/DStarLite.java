package com.mattymatty.NPCNavigator.DStarLite;


import com.mattymatty.NPCNavigator.Graph.Cell;
import com.mattymatty.NPCNavigator.Graph.Movement;
import com.mattymatty.NPCNavigator.Graph.Movements.NullMovement;
import com.mattymatty.NPCNavigator.NPCNavigator;
import com.mattymatty.NPCNavigator.Utils;
import org.bukkit.Location;

import java.util.*;
import java.util.stream.Collectors;


public class DStarLite {

    List<Movement> curPath = new LinkedList<>();
    List<Movement> path = Collections.emptyList();

    private State s_start = new State();
    private State s_goal = new State();
    private State s_last = new State();

    private int maxCalc = 0;
    private double k_m = 0;

    private PriorityQueue<State> openList = new PriorityQueue<State>();
    private HashSet<State> openHash = new HashSet<>();

    public HashMap<State, CellInfo> cellMap = new HashMap<State, CellInfo>();


    public DStarLite() {
        maxCalc = 80000;
    }

    public void init(Location start,Location goal) {
        cellMap.clear();
        curPath.clear();
        openList.clear();
        openHash.clear();

        s_start.setLocation(start);
        s_goal.setLocation(goal);

        if(path.size()==0)
            path = List.of(new NullMovement(getCell(s_start)));

        k_m = 0;

        setRHS(s_goal,0);

        s_last = s_start;

        s_goal.k.setLeft(heuristic(s_start, s_goal));
        s_goal.k.setRight(0.0);

        openList.add(s_goal);
        openHash.add(s_goal);
    }

    /*
     * Update the position of the agent/robot.
     * This does not force a replan.
     */
    public void updateStart(Location start)
    {
        s_start.setLocation(start);

        k_m += heuristic(s_last,s_start);

        calculateKey(s_start);
        s_last = s_start;

    }

    public void updateEdge(Movement mov)
    {
        State u = new State(mov.getOrigin());
        State v = new State(mov.getDest());
        if(mov.getOldCost() > mov.getCost()){
            if(u.neq(s_goal)){
                setRHS(u,Math.min(getRHS(u),mov.getCost() + getG(v)));
            }
        }else if (getRHS(u) == mov.getOldCost() + getG(v)){
            if(u.neq(s_goal)){
                setRHS(u,minRHSSuccesssors(u));
            }
        }
        updateVertex(u);
    }

    private State calculateKey(State u) {
        double val = Math.min(getRHS(u), getG(u));

        u.k.setLeft(val + heuristic(u, s_start) + k_m);
        u.k.setRight(val);

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
        return (NPCNavigator.instance.approxCost)? Utils.approx3dDistance(a.getVector(), b.getVector()): Utils.trueDist3D(a.getVector(),b.getVector());
    }

    HashMap<Location,Cell> snapshots = new HashMap<>();


    public boolean replan() {
        curPath.clear();
        snapshots.clear();

        int res = computeShortestPath();
        if (res < 0) {
            System.out.println("No Path to Goal");
            return false;
        }

        List<Movement> n;
        State cur = s_start;

        /*
        if (getG(s_start) == Double.POSITIVE_INFINITY) {
            System.out.println("No Path to Goal");
            return false;
        }*/

        while (cur.neq(s_goal)) {
            n = getSucc(cur);

            if (n.isEmpty()) {
                System.out.println("No Path to Goal");
                return false;
            }

            double cmin = Double.POSITIVE_INFINITY;
            double tmin = 0;
            State smin = new State();
            Movement mmin = null;

            for (Movement m : n) {
                State i = new State(m.getDest());
                double val = m.getCost();
                double val2 = Utils.trueDist3D(i.getVector(), s_goal.getVector()) + Utils.trueDist3D(s_start.getVector(), i.getVector());
                val += getG(i);

                if (close(val, cmin)) {
                    if (tmin > val2) {
                        tmin = val2;
                        cmin = val;
                        smin = i;
                        mmin = m;
                    }
                } else if (val < cmin) {
                    tmin = val2;
                    cmin = val;
                    smin = i;
                    mmin = m;
                }
            }
            n.clear();
            if(smin.getLocation().getWorld() == null){
                System.out.println("No Path to Goal");
                return false;
            }
            cur = new State(smin);
            curPath.add(mmin);
            //cur = smin;
        }
        snapshots.clear();
        path = List.copyOf(curPath);
        return true;
    }

    /*
     * As per [S. Koenig,2002]
     */
    private int computeShortestPath() {
        List<Movement> mov;

        if (openList.isEmpty()) return 1;

        int k = 0;
        while ((!openList.isEmpty()) && (
                (openList.peek().lt(calculateKey(s_start))) ||
                        (getRHS(s_start) > getG(s_start))
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

                mov = getPred(u);
                for (Movement m : mov) {
                    State i = new State(m.getOrigin());
                    if (i.neq(s_goal)) {
                        setRHS(i, Math.min(getRHS(i), m.getCost() + getG(u)));
                    }
                    updateVertex(i);
                }
            } else {                         // g <= rhs, state has got worse
                double old_g = getG(u);
                setG(u, Double.POSITIVE_INFINITY);
                mov = getPred(u);
                mov.add(new NullMovement(getCell(u)));
                for (Movement m : mov) {
                    State i = new State(m.getOrigin());
                    if (getRHS(i) == m.getCost() + old_g) {
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
        List<Movement> s = getSucc(u);
        double min = Double.POSITIVE_INFINITY;
        double tmp;

        for (Movement m : s) {
            State i = new State(m.getDest());
            tmp = getG(i) + m.getCost();
            if (tmp < min)
                min = tmp;
        }
        return min;
    }

    private void updateVertex(State u) {
        if (openHash.contains(u)) {
            if (getG(u) != getRHS(u)) {
                openHash.remove(u);
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

    private Cell getCell(State u){
        Cell snapshot = snapshots.get(u.getLocation());
        if(snapshot == null){
            snapshot = Cell.getCell(u.getLocation()).clone();
            snapshots.put(u.getLocation(),snapshot);
        }
        return snapshot;
    }

    /*
     * Returns a list of all the predecessor states for state u. Since
     * this is for an 8-way connected graph, the list contains all the
     * neighbours for state u. Occupied neighbours are not added to the list
     */
    private List<Movement> getPred(State u) {
        return getCell(u).getInMovements().stream().filter(Movement::isValid).collect(Collectors.toList());
    }

    /*
     * Returns a list of successor states for state u, since this is an
     * 8-way graph this list contains all of a cells neighbours. Unless
     * the cell is occupied, in which case it has no successors.
     */
    private List<Movement> getSucc(State u) {
        return getCell(u).getOutMovements().stream().filter(Movement::isValid).collect(Collectors.toList());
    }

    /*
     * Returns true if x and y are within 10E-5, false otherwise
     */
    private boolean close(double x, double y) {
        if (x == Double.POSITIVE_INFINITY && y == Double.POSITIVE_INFINITY) return true;
        return (Math.abs(x - y) < 0.00001);
    }

    public List<Movement> getCurPath() {
        return Collections.unmodifiableList(curPath);
    }

}
