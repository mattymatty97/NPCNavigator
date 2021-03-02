package com.mattymatty.NPCNavigator.Graph;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Set;

public abstract class Movement {

    private static final Cache<Triple<Location,Location,Class<?>>,Movement> movementCache = CacheBuilder.newBuilder().weakValues().build();

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

    public abstract boolean update();

    public abstract boolean update(boolean propagate);

    @Override
    public abstract Movement clone();
}
