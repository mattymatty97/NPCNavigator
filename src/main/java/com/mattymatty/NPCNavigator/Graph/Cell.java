package com.mattymatty.NPCNavigator.Graph;

import com.google.common.cache.*;
import org.bukkit.Location;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public abstract class Cell implements Cloneable,Updatable{

    protected static final LoadingCache<Location, Cell> cellCache1 = CacheBuilder.newBuilder().weakValues()
            .build(new CacheLoader<Location, Cell>() {
                @Override
                @ParametersAreNonnullByDefault
                public Cell load(Location key) throws Exception {
                    return new BlockCell(key);
                }
            });

    protected static final LoadingCache<Location, Cell> cellCache2 = CacheBuilder.newBuilder().softValues()
            .expireAfterAccess(1, TimeUnit.MINUTES)
            .removalListener((RemovalListener<Location, Cell>) notification -> {
                cellCache1.invalidate(notification.getKey());
                Cell cell = notification.getValue();
                if(cell!=null) {
                    cell.reset();
                    cell.getInMovements().forEach(Updatable::reset);
                    cell.getOutMovements().forEach(Updatable::reset);
                }
            })
            .build(new CacheLoader<Location, Cell>() {
                @Override
                @ParametersAreNonnullByDefault
                public Cell load(Location key) throws Exception {
                    return cellCache1.get(key);
                }
            });

    public static Object getLock(){
        return cellCache2;
    }

    public static boolean cellExist(Location loc){
        return cellCache2.asMap().containsKey(loc);
    }

    public static void doCellCleanup(){
        synchronized (cellCache2){
            cellCache2.cleanUp();
            cellCache1.cleanUp();
        }
    }

    public static Cell getCell(Location loc){
        synchronized (cellCache2){
            return cellCache2.getUnchecked(loc);
        }
    };

    public static Collection<Cell> getCachedCells(){
        synchronized (cellCache2){
            return cellCache2.asMap().values();
        }
    };

    public static Cell getCellSilently(Location loc){
        synchronized (cellCache2){
            return cellCache1.getIfPresent(loc);
        }
    };

    public abstract Location getLocation();

    public abstract boolean isValid();

    public abstract Set<Movement> getInMovements();

    public abstract Set<Movement> getOutMovements();

    public boolean update(){
        return update(0, new HashSet<>());
    }
    public boolean update(int dept, Set<Updatable> visited){return update(dept,visited,false);}
    public abstract boolean update(int dept, Set<Updatable> visited,boolean silent);

    public abstract Cell clone();

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        getOutMovements().forEach(Updatable::reset);
        getInMovements().forEach(Updatable::reset);
    }
}
