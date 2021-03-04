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

    private State s_start = new State();
    private State s_goal = new State();

    private int maxCalc = 0;
    private double k_m = 0;

    private final Object lock = new Object();
    private boolean closed = false;

    private  PriorityQueue<State> cellQueue = new PriorityQueue<>();
    private  Set<State> cellHash = new HashSet<>();

    public HashMap<State, CellInfo> cellMap = new HashMap<State, CellInfo>();

    private AtomicBoolean update = new AtomicBoolean(false);


    public DStarLite() {
        maxCalc = 80000;
    }

    public void end(){
        synchronized (lock) {
            cellQueue.clear();
            cellHash.clear();
            cellMap.clear();
            cellQueue = null;
            cellHash = null;
            cellMap = null;
            closed = true;
        }
    }

    public void init(Location start, Location goal) {
        if(closed)
            return;
        synchronized (cellQueue) {
            cellQueue.clear();
            cellHash.clear();
            k_m = 0;
            cellMap.clear();
            s_goal = new State(goal);
            s_start = new State(start);
            setRHS(s_goal, 0);
            updateKey(s_goal);
            updateKey(s_start);
            cellQueue.add(s_goal);
            cellHash.add(s_goal);
            update.set(true);
        }
    }

    public void updateEdge(Movement edge,double oldCost){
        synchronized (lock) {
            if (closed)
                return;
            State u = new State(edge.getOrigin());
            State v = new State(edge.getDest());
            synchronized (cellQueue) {
                if (cellMap.containsKey(u)) {
                    if (oldCost > edge.getCost()) {
                        if (u.neq(s_goal)) setRHS(u, Math.min(getRHS(u), edge.getCost() + getG(v)));
                    } else if (getRHS(u) == oldCost + getG(v)) {
                        if (u.neq(s_goal)) setRHS(u, minRHSSuccessors(u));
                    }
                    updateVertex(u);
                    update.set(true);
                }
            }
        }
    }

    public void updateStart(Location loc){
        if(closed)
            return;
        synchronized (cellQueue) {
            State snext = new State(loc);
            k_m += heuristic(s_start, snext);
            s_start = snext;
        }
    }

    public Movement getNext(){
        synchronized (lock) {
            if (closed)
                return new NullMovement(getCurr());
            ;
            synchronized (cellQueue) {
                List<Movement> s = getSucc(s_start);
                double cmin = Double.POSITIVE_INFINITY;
                Movement mmin = new NullMovement(s_start.getLocation());
                double ctmp;

                for (Movement m : s) {
                    State i = new State(m.getDest());
                    ctmp = getG(i) + m.getCost();
                    if (ctmp < cmin) {
                        cmin = ctmp;
                        mmin = m;
                    }
                }
                return mmin;
            }
        }
    }

    public Location getCurr(){
        return s_start.getLocation();
    }
    public Location getGoal(){
        return s_goal.getLocation();
    }

    public boolean needsUpdate(){
        return update.get();
    }

    private void updateVertex(State u){
        double g = getG(u);
        double rhs = getRHS(u);
        if(cellHash.contains(u)){
            if(g!=rhs){
                updateKey(u);
                cellQueue.remove(u);
                cellQueue.add(u);
            }else{
                cellQueue.remove(u);
                cellHash.remove(u);
            }
        }else{
            if(g!=rhs){
                updateKey(u);
                cellQueue.add(u);
                cellHash.add(u);
            }
        }
    }

    public int computeShortestPath(){
        synchronized (lock) {
            if(closed)
                return -5;
            if (cellQueue.isEmpty()) return 1;

            int k = 0;

            //clean the queue
            while (!cellQueue.isEmpty()) {
                State u = cellQueue.peek();
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
                    State u = cellQueue.poll();
                    assert u != null : "list was empty";
                    State k_old = new State(u);
                    updateKey(u);

                    if (k_old.lt(u)) {
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
                                State s = new State(m.getOrigin());
                                if (s.neq(s_goal)) {
                                    setRHS(s, Math.min(getRHS(s), m.getCost() + g_u));
                                }
                                updateVertex(s);
                            }
                        } else {
                            setG(u, Double.POSITIVE_INFINITY);
                            List<Movement> pred = getPred(u);
                            pred.add(new NullMovement(u.getLocation()));
                            for (Movement m : pred) {
                                State s = new State(m.getOrigin());
                                if (getRHS(s) == m.getCost() + g_u) {
                                    if (s.neq(s_goal)) {
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
    }

    private boolean loopCheck() {
        synchronized (cellQueue){
            return !cellQueue.isEmpty() && (cellQueue.peek().lt(updateKey(s_start)) || getRHS(s_start) > getG(s_start));
        }
    }

    /*
     * Returns the heuristic value between two states
     */
    private double heuristic(State a, State b) {
        return (NPCNavigator.instance.approxCost)? Utils.approx3dDistance(a.getVector(), b.getVector()): Utils.trueDist3D(a.getVector(),b.getVector());
    }

    private State updateKey(State u) {
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


    private Cell getCell(State u){
        return Cell.getCell(u.getLocation());
    }


    private List<Movement> getPred(State u) {
        return getCell(u).getInMovements().stream().filter(Movement::isValid).collect(Collectors.toList());
    }

    private List<Movement> getSucc(State u) {
        return getCell(u).getOutMovements().stream().filter(Movement::isValid).collect(Collectors.toList());
    }

    private double minRHSSuccessors(State u) {
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
}
