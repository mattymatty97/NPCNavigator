package com.mattymatty.NPCNavigator.Graph;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.apache.commons.lang3.tuple.Triple;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public abstract class Movement implements Cloneable, Updatable {

    private static final Cache<Triple<Location,Location,Class<?>>,Movement> movementCache = CacheBuilder.newBuilder().weakValues().build();

    private static final HashMap<Class<? extends Movement>,Set<Class<? extends Cell>>> cellMovClassMap = new HashMap<>();

    private static final HashMap<Class<? extends Cell>,Set<UpdateListener>> listenersMap = new HashMap<>();
    private static final HashMap<UpdateListener,Set<Class<? extends Cell>>> reverseMap = new HashMap<>();

    protected static void registerType(Class<? extends Cell> cellType,Set<Class<? extends Movement>> movements){
        for (Class<? extends Movement> c : movements)
            cellMovClassMap.computeIfAbsent(c,(mov)->new HashSet<>()).add(cellType);
    }

    public static UpdateListener addListener(Class<? extends Cell> cellType, UpdateListener listener){
        listenersMap.computeIfAbsent(cellType,(key)->new HashSet<>()).add(listener);
        reverseMap.computeIfAbsent(listener,(key)->new HashSet<>()).add(cellType);
        return listener;
    }

    public static UpdateListener removeListener(UpdateListener listener){
        Set<Class<? extends Cell>> classes = reverseMap.get(listener);
        if(classes!=null){
            for(Class c : classes){
                listenersMap.get(c).remove(listener);
            }
            reverseMap.remove(listener);
            return listener;
        }
        return null;
    }

    protected static void emit(Movement mov){
        Set<Class<? extends Cell>> cells = cellMovClassMap.get(mov.getClass());
        if(cells!=null){
            for (Class c: cells){
                Set<UpdateListener> listeners = listenersMap.get(c);
                listeners.forEach(l->l.accept(mov));
            }
        }
    }

    public static Movement checkCache(Movement toCheck){
        Triple<Location,Location,Class<?>> curr = new ImmutableTriple<>(toCheck.getOrigin(),toCheck.getDest(),toCheck.getClass());
        Movement cached = movementCache.getIfPresent(curr);
        if(cached==null) {
            cached = toCheck;
            movementCache.put(curr,cached);
        }
        return cached;
    }

    public abstract Vector getVector();

    public abstract Location getOrigin();

    public abstract Location getDest();

    public abstract double getCost();

    public abstract boolean isValid();

    public abstract Set<Block> getAffectedBlocks();

    private double oldCost = Double.POSITIVE_INFINITY;

    public double getOldCost() {
        return oldCost;
    }

    protected void saveCost(){
        oldCost = getCost();
    }

    @Override
    public abstract boolean update();

    @Override
    public abstract boolean update(int dept, Set<Updatable> visited);

    @Override
    public abstract Movement clone();
}
