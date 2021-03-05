package com.mattymatty.NPCNavigator.DStarLite;

import com.mattymatty.NPCNavigator.Graph.Cell;
import com.mattymatty.NPCNavigator.Graph.Movement;
import com.mattymatty.NPCNavigator.Graph.Movements.NullMovement;
import com.mattymatty.NPCNavigator.NPCNavigator;
import com.mattymatty.NPCNavigator.Utils;
import org.bukkit.Location;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class DStarLite {

    private Location s_start = null;
    private Location s_goal = null;

    private int maxCalc = 0;
    private double k_m = 0;

    private final Object lock = new Object();
    private boolean closed = false;

    private PriorityQueue<Location> cellQueue = new PriorityQueue<>(Comparator.comparing(this::getKey));
    private Set<Location> cellHash = new HashSet<>();

    public HashMap<Location, Info> infoMap = new HashMap<>();
    public HashMap<Location, Key> keyMap = new HashMap<>();

    private AtomicBoolean update = new AtomicBoolean(false);


    public DStarLite() {
        maxCalc = 80000;
    }

    public void end(){
            cellQueue.clear();
            cellHash.clear();
            infoMap.clear();
            keyMap.clear();
            cellQueue = null;
            cellHash = null;
            infoMap = null;
            keyMap = null;
            closed = true;
    }

    public void init(Location start, Location goal) {
        if(closed)
            return;
        synchronized (cellQueue) {
            cellQueue.clear();
            cellHash.clear();
            k_m = 0;
            infoMap.clear();
            keyMap.clear();
            s_goal = goal.clone();
            s_start = start.clone();
            setRHS(s_goal, 0);
            calculateKey(s_goal);
            calculateKey(s_start);
            cellQueue.add(s_goal);
            cellHash.add(s_goal);
            update.set(true);
        }
    }

    public void updateEdge(Movement edge,double oldCost){
            if (closed)
                return;
            Location u = edge.getOrigin();
            Location v = edge.getDest();
            synchronized (cellQueue) {
                if (infoMap.containsKey(u)) {
                    if (oldCost > edge.getCost()) {
                        if (!u.equals(s_goal)) setRHS(u, Math.min(getRHS(u), edge.getCost() + getG(v)));
                    } else if (getRHS(u) == oldCost + getG(v)) {
                        if (!u.equals(s_goal)) setRHS(u, minRHSSuccessors(u));
                    }
                    updateVertex(u);
                    update.set(true);
                }
            }
    }

    public void updateStart(Location loc){
        if(closed)
            return;
        synchronized (cellQueue) {
            k_m += heuristic(s_start, loc);
            s_start = loc;
        }
    }

    public Movement getNext(){
            if (closed || getRHS(s_start) == Double.POSITIVE_INFINITY)
                return new NullMovement(getCurr());
            ;
            synchronized (cellQueue) {
                List<Movement> s = getSucc(s_start);
                double cmin = Double.POSITIVE_INFINITY;
                double tmin = Double.POSITIVE_INFINITY;
                Movement mmin = new NullMovement(s_start);
                double ctmp;
                double ttmp;

                for (Movement m : s) {
                    Location i = m.getDest();
                    ctmp = getG(i) + m.getCost();

                    if(close(cmin,ctmp)){
                        ttmp = getTtmp(m);
                        if(ttmp < tmin){
                            cmin = ctmp;
                            tmin = ttmp;
                            mmin = m;
                        }
                    }else if (ctmp < cmin) {
                        cmin = ctmp;
                        tmin = getTtmp(m);
                        mmin = m;
                    }
                }
                return mmin;
            }
    }

    /*
     * Returns true if x and y are within 10E-5, false otherwise
     */
    private boolean close(double x, double y)
    {
        if (x == Double.POSITIVE_INFINITY && y == Double.POSITIVE_INFINITY) return true;
        return (Math.abs(x-y) < 0.00001);
    }

    private double getTtmp(Movement m) {
        return Utils.trueDist3D(m.getDest().toVector(), s_start.toVector()) + Utils.trueDist3D(m.getDest().toVector(), s_goal.toVector());
    }

    public Location getCurr(){
        return s_start.clone();
    }
    public Location getGoal(){
        return s_goal.clone();
    }

    public boolean needsUpdate(){
        return update.get();
    }

    private void updateVertex(Location u){
        double g = getG(u);
        double rhs = getRHS(u);
        if(cellHash.contains(u)){
            if(g!=rhs){
                calculateKey(u);
                cellQueue.remove(u);
                cellQueue.add(u);
            }else{
                cellQueue.remove(u);
                cellHash.remove(u);
            }
        }else{
            if(g!=rhs){
                calculateKey(u);
                cellQueue.add(u);
                cellHash.add(u);
            }
        }
    }

    public int computeShortestPath(){
            if(closed) {
                update.set(false);
                return -5;
            }
            if (cellQueue.isEmpty()) {
                update.set(false);
                return 1;
            }

            int k = 0;

            //clean the queue
            while (!cellQueue.isEmpty()) {
                Location u = cellQueue.peek();
                if (cellHash.contains(u))
                    break;
                else
                    cellQueue.poll();
            }

            boolean test = loopCheck();
            while (test) {
                if (k++ > maxCalc) {
                    System.out.println("At Max Try");
                    update.set(false);
                    return -1;
                }
                synchronized (cellQueue) {
                    if (!loopCheck())
                        break;
                    Location u = cellQueue.poll();
                    assert u != null : "list was empty";
                    Key k_old = new Key(getKey(u));
                    Key k_new = calculateKey(u);

                    if (k_old.lt(k_new)) {
                        cellQueue.add(u);
                    } else {
                        double g_u = getG(u);
                        double rhs_u = getRHS(u);
                        if (g_u > rhs_u) {
                            g_u = rhs_u;
                            setG(u, rhs_u);
                            cellHash.remove(u);
                            List<Movement> pred = getPred(u);
                            for (Movement m : pred) {
                                Location s = m.getOrigin();
                                if (!s.equals(s_goal)) {
                                    setRHS(s, Math.min(getRHS(s), m.getCost() + g_u));
                                }
                                updateVertex(s);
                            }
                        } else {
                            setG(u, Double.POSITIVE_INFINITY);
                            List<Movement> pred = getPred(u);
                            pred.add(new NullMovement(u));
                            for (Movement m : pred) {
                                Location s = m.getOrigin();
                                if (getRHS(s) == m.getCost() + g_u) {
                                    if (!s.equals(s_goal)) {
                                        setRHS(s, minRHSSuccessors(s));
                                    }
                                }
                                updateVertex(s);
                            }
                        }
                    }
                }
                Thread.yield();
                test = loopCheck();
            }
            update.set(false);
            return 0;
    }

    private boolean loopCheck() {
        synchronized (cellQueue){
            if(cellQueue.isEmpty())
               return false;
            Location top = cellQueue.peek();
            Key topKey = getKey(top);
            if(topKey.lt(calculateKey(s_start)))
                return true;
            return getRHS(s_start) > getG(s_start);
        }
    }

    /*
     * Returns the heuristic value between two states
     */
    private double heuristic(Location a, Location b) {
        return (NPCNavigator.approxCost)? Utils.approx3dDistance(a.toVector(), b.toVector()): Utils.trueDist3D(a.toVector(),b.toVector());
    }

    private Key calculateKey(Location u) {
        double val = Math.min(getRHS(u), getG(u));
        Key key = getKey(u);
        key.setLeft(val + heuristic(u, s_start) + k_m);
        key.setRight(val);

        return key;
    }

    /*
     * Returns the rhs value for state u.
     */
    private double getRHS(Location u) {
        Info cell = infoMap.get(u);

        if (cell == null)
            return Double.POSITIVE_INFINITY;
        return cell.rhs;
    }

    /*
     * Returns the g value for the state u.
     */
    private double getG(Location u) {
        Info cell = infoMap.get(u);

        if (cell == null)
            return Double.POSITIVE_INFINITY;
        return cell.g;
    }

    /*
     * Sets the G value for state u
     */
    private void setG(Location u, double g) {
        getInfo(u).g = g;
    }

    /*
     * Sets the rhs value for state u
     */
    private void setRHS(Location u, double rhs) {
        getInfo(u).rhs = rhs;
    }

    /*
     * Checks if a cell is in the hash table, if not it adds it in.
     */
    private Info getInfo(Location u) {
        return infoMap.computeIfAbsent(u,k->new Info());
    }


    private Cell getCell(Location u){
        return Cell.getCell(u);
    }

    private Key getKey(Location u){
        return keyMap.computeIfAbsent(u,(k)->new Key(Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY));
    }


    private List<Movement> getPred(Location u) {
        return getCell(u).getInMovements().stream().filter(Movement::isValid).collect(Collectors.toList());
    }

    private List<Movement> getSucc(Location u) {
        return getCell(u).getOutMovements().stream().filter(Movement::isValid).collect(Collectors.toList());
    }

    private double minRHSSuccessors(Location u) {
        List<Movement> s = getSucc(u);
        double min = Double.POSITIVE_INFINITY;
        double tmp;

        for (Movement m : s) {
            Location i = m.getDest();
            tmp = getG(i) + m.getCost();
            if (tmp < min)
                min = tmp;
        }
        return min;
    }
}
