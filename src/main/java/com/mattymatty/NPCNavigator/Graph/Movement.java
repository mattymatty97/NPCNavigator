package com.mattymatty.NPCNavigator.Graph;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.apache.commons.lang3.tuple.Triple;

import java.util.*;

public abstract class Movement implements Cloneable, Updatable {

    private static final Cache<Triple<Location,Location,Class<? extends Movement>>,Movement> movementCache = CacheBuilder.newBuilder().weakValues().build();


    private static final Set<UpdateListener> listenersSet = new HashSet<>();

    public static UpdateListener addListener(UpdateListener listener){
        listenersSet.add(listener);
        return listener;
    }

    public static UpdateListener removeListener(UpdateListener listener){
        listenersSet.remove(listener);
        return null;
    }

    protected static void emit(Movement mov){
        listenersSet.forEach(l->l.accept(mov));
    }

    public static Movement checkCache(Movement toCheck){
        synchronized (movementCache) {
            Triple<Location, Location, Class<? extends Movement>> curr = new ImmutableTriple<>(toCheck.getOrigin(), toCheck.getDest(), toCheck.getClass());
            Movement cached = movementCache.getIfPresent(curr);
            if (cached == null) {
                cached = toCheck;
                movementCache.put(curr, cached);
            }
            return cached;
        }
    }

    public static Collection<Movement> getCachedMovements(){
        synchronized (movementCache) {
            return movementCache.asMap().values();
        }
    }

    public static void doMovementCleanup(){
        synchronized (movementCache) {
            movementCache.cleanUp();
        }
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

    @Override
    public abstract boolean update();

    @Override
    public abstract boolean update(int dept, Set<Updatable> visited);

    @Override
    public abstract boolean update(int dept, Set<Updatable> visited,boolean silent);

    @Override
    public abstract Movement clone();
}
