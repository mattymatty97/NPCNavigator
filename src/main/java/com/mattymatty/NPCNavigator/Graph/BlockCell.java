package com.mattymatty.NPCNavigator.Graph;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mattymatty.NPCNavigator.Graph.Movements.*;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Door;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BlockCell extends Cell {

    public static final LoadingCache<Location, Cell> cellCache = CacheBuilder.newBuilder().softValues()
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build(new CacheLoader<>() {
                @Override
                public BlockCell load(Location key) throws Exception {
                    return new BlockCell(key);
                }
            });

    static {
        Movement.registerType(BlockCell.class,Set.of(
                EastMovement.class,
                NorthMovement.class,
                WestMovement.class,
                SouthMovement.class
        ));
    }

    private final Location pos;
    private boolean valid;
    private final Set<Movement> inMovements;
    private final Set<Movement> outMovements;
    private Set<Block> affectedBlocks;

    public static Object getLock(){
        return cellCache;
    }

    public static Cell getCell(Location loc) {
        Cell cell;
        synchronized (cellCache) {
            cell = cellCache.getUnchecked(loc);
        }
        return cell;
    }


    public Location getLocation() {
        return pos.clone();
    }

    public boolean isValid() {
        return valid;
    }

    public Set<Movement> getInMovements() {
        return Collections.unmodifiableSet(inMovements);
    }

    public Set<Movement> getOutMovements() {
        return Collections.unmodifiableSet(outMovements);
    }

    public Set<Block> getAffectedBlocks() {
        return Collections.unmodifiableSet(affectedBlocks);
    }

    public BlockCell(Location pos) {
        this.pos = pos;
        valid = true;
        inMovements = Set.of(
                Movement.checkCache(new EastMovement(this,true)),
                Movement.checkCache(new WestMovement(this,true)),
                Movement.checkCache(new SouthMovement(this,true)),
                Movement.checkCache(new NorthMovement(this,true))
        );
        outMovements = Set.of(
                Movement.checkCache(new EastMovement(this,false)),
                Movement.checkCache(new WestMovement(this,false)),
                Movement.checkCache(new SouthMovement(this,false)),
                Movement.checkCache(new NorthMovement(this,false))
        );
        affectedBlocks = Collections.emptySet();
    }

    protected BlockCell(BlockCell toClone) {
        this.pos = toClone.pos;
        this.valid = toClone.valid;
        this.inMovements = toClone.inMovements.stream().map(Movement::clone).collect(Collectors.toSet());
        this.outMovements = toClone.outMovements.stream().map(Movement::clone).collect(Collectors.toSet());
        this.affectedBlocks = toClone.getAffectedBlocks();
    }

    @Override
    public boolean update(int dept, Set<Updatable> visited){
        if(visited.contains(this))
            return false;
        visited.add(this);
        boolean updated;
        Block top = pos.clone().add(0, 1, 0).getBlock();
        Block bottom = pos.getBlock();
        Block ground = pos.clone().add(0, -1, 0).getBlock();

        affectedBlocks = Set.of(top, bottom, ground);

        if (
                (top.isPassable() || top.getType().data == Door.class) &&
                        (bottom.isPassable() || bottom.getType().data == Door.class) &&
                        !ground.isPassable()
        ) {
            updated = !valid;
            valid = true;
        } else {
            updated = valid;
            valid = false;
        }

        updated = updated | inMovements.stream().map((m) -> m.update(dept,visited)).anyMatch((p) -> p);
        updated = updated | outMovements.stream().map((m) -> m.update(dept,visited)).anyMatch((p) -> p);

        return updated;
    }

    @Override
    public BlockCell clone(){
        return new BlockCell(this);
    }
}
