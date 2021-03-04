package com.mattymatty.NPCNavigator.DStarLite;

import com.mattymatty.NPCNavigator.Graph.Cell;
import com.mattymatty.NPCNavigator.Graph.Movement;
import com.mattymatty.NPCNavigator.Graph.Movements.NullMovement;
import com.mattymatty.NPCNavigator.Graph.UpdateListener;
import org.bukkit.Location;

import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DStarLitePathfinder{
    private static int threadCount = 0;
    private static final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),r -> {
        final Thread thread = new Thread(r);
        thread.setUncaughtExceptionHandler((t, e) -> {
            System.out.println("Uncaught Exception occurred on thread: " + t.getName());
            System.out.println("Exception message: " + e.getMessage());
        });
        thread.setName("Pathing thread -" + threadCount++);
        return thread;
    });

    public static DStarLitePathfinder newDefaultPathfinder(){
        return new DStarLitePathfinder();
    }

    private DStarLite instance;
    private final Object lock = new Object();
    private Status status = Status.Idle;

    public DStarLitePathfinder() {
        instance = new DStarLite();
    }

    public DStarLitePathfinder init(Location start, Location goal){
        if(getStatus()==Status.Idle) {
            start.setPitch(0);
            goal.setPitch(0);
            start.setYaw(0);
            goal.setYaw(0);
            instance.init(start, goal);
            if (current == null)
                current = start;
        }
        return this;
    }

    UpdateListener listener;

    public DStarLitePathfinder start(){
        if(getStatus()==Status.Idle) {
            listener = Movement.addListener((mov)-> instance.updateEdge(mov, mov.getOldCost()));
            update();
        }
        return this;
    }

    private void update() {
        synchronized (lock) {
            if (status == Status.Idle || status == Status.Ready) {
                setStatus(Status.Queued);
                executor.submit(() -> {
                    try {
                        setStatus(Status.Calculating);
                        instance.computeShortestPath();
                        synchronized (lock) {
                            setStatus(Status.Ready);
                            current = instance.getCurr();
                        }
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                });
            }
        }
    }


    private Location current;

    private Location getCurrent(){
        synchronized (lock) {
            return current;
        }
    }

    public Location next(){
        if(getStatus()==Status.Ready){
                synchronized (lock) {
                    Movement next = instance.getNext();
                    if(next instanceof NullMovement) {
                        setStatus(Status.Done);
                        return null;
                    }
                    instance.updateStart(current);
                    Cell nextCell = Cell.getCell(next.getDest());
                    if (makeUpdate(nextCell)) {
                        update();
                    } else {
                        current = nextCell.getLocation();
                        if(next.getDest().equals(instance.getGoal()))
                            setStatus(Status.Done);
                    }
                }
        }
        return current;
    }

    private boolean makeUpdate(Cell cell){
        synchronized (Cell.getLock()){
            return cell.update(0,new HashSet<>());
        }
    }

    public Status getStatus(){
        synchronized (lock) {
            return status;
        }
    }

    private Status setStatus(Status status){
        synchronized (lock) {
            if(status==Status.Done) {
                Movement.removeListener(listener);
                instance.end();
            }
            return this.status = status;
        }
    }

    public enum Status{
        Idle,
        Queued,
        Calculating,
        Ready,
        Done
    }

}
