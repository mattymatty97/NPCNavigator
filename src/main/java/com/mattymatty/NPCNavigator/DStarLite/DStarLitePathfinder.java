package com.mattymatty.NPCNavigator.DStarLite;

import com.google.common.cache.Cache;
import com.mattymatty.NPCNavigator.Graph.BlockCell;
import com.mattymatty.NPCNavigator.Graph.Cell;
import com.mattymatty.NPCNavigator.Graph.Movement;
import com.mattymatty.NPCNavigator.Graph.UpdateListener;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class DStarLitePathfinder<T extends Cell>{
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
        return new DStarLitePathfinder<>(BlockCell.class);
    }

    private DStarLite<T> instance;
    private final Object lock = new Object();
    private Status status = Status.Idle;
    private Class<T> cellType;

    public DStarLitePathfinder(Class<T> cellType) {
        instance = new DStarLite<T>();
        this.cellType = cellType;
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
            listener = Movement.addListener(cellType,(mov)-> updatedMoves.computeIfAbsent(mov,Movement::getOldCost));
            update();
        }
        return this;
    }

    private final HashMap<Movement,Double> updatedMoves = new HashMap<>();

    private void update() {
        synchronized (lock) {
            if (status != Status.Queued && status != Status.Calculating) {
                setStatus(Status.Queued);
                executor.submit(() -> {
                    try {
                        setStatus(Status.Calculating);
                        Map<Location,Cell> snapshots;
                        synchronized (T.getLock()){
                            snapshots = ((Cache<Location, Cell>)T.getLock()).asMap().entrySet().parallelStream().collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    (Map.Entry<Location,Cell> e)->e.getValue().clone()
                            ));
                            updatedMoves.forEach((m,cost)->{
                                instance.updateEdge(m,cost);
                            });
                            updatedMoves.clear();
                        }
                        instance.replan(snapshots);
                        synchronized (lock) {
                            setStatus(Status.Ready);
                            current = instance.path.get(0).getOrigin();
                            currentIndex = 0;
                        }
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                });
            }
        }
    }


    private Location current;
    private int currentIndex = 0;

    private Location getCurrent(){
        synchronized (lock) {
            return current;
        }
    }

    public Location current(){
        return getCurrent();
    }

    public Location next(){
        if(getStatus()==Status.Ready){
            if(instance.path.size() > 0) {
                synchronized (lock) {
                    instance.updateStart(current);
                    if(currentIndex+1 < instance.path.size()) {
                        Cell nextCell = BlockCell.getCell(instance.path.get(currentIndex++).getDest());
                        if (makeUpdate(nextCell)) {
                            update();
                        } else {
                            current = nextCell.getLocation();
                        }
                    }else{
                        setStatus(Status.Done);
                        instance = null;
                        System.gc();
                    }
                }
            }else{
                return null;
            }
        }
        return current;
    }

    private boolean makeUpdate(Cell cell){
        synchronized (T.getLock()){
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
            if(status==Status.Done)
                Movement.removeListener(listener);
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
