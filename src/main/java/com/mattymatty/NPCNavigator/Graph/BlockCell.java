package com.mattymatty.NPCNavigator.Graph;

import com.mattymatty.NPCNavigator.Graph.Movements.*;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Door;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class BlockCell extends Cell {

    private final Location pos;
    private boolean valid;
    private final Set<Movement> inMovements;
    private final Set<Movement> outMovements;
    private Set<Block> affectedBlocks;



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

    public boolean update(boolean propagate){
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
        if(propagate) {
            updated = updated | inMovements.stream().map((m) -> m.update(false)).anyMatch((p) -> p);
            updated = updated | outMovements.stream().map((m) -> m.update(false)).anyMatch((p) -> p);
        }
        return updated;
    }

    @Override
    public BlockCell clone(){
        return new BlockCell(this);
    }
}