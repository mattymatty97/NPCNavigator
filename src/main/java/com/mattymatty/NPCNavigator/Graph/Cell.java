package com.mattymatty.NPCNavigator.Graph;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public abstract class Cell  implements Cloneable,Updatable{

    protected static final LoadingCache<Location, Cell> cellCache = CacheBuilder.newBuilder().softValues()
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build(new CacheLoader<Location, Cell>() {
                @Override
                public BlockCell load(Location key) throws Exception {
                    return new BlockCell(key);
                }
            });;

    public static Object getLock(){
        return cellCache;
    }

    public static Cell getCell(Location loc){
        synchronized (cellCache){
            return cellCache.getUnchecked(loc);
        }
    };

    public abstract Location getLocation();

    public abstract boolean isValid();

    public abstract Set<Movement> getInMovements();

    public abstract Set<Movement> getOutMovements();

    public boolean update(){
        return update(0, new HashSet<>());
    }
    public abstract boolean update(int dept, Set<Updatable> visited);

    public abstract Cell clone();

}
