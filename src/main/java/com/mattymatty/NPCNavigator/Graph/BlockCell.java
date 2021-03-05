package com.mattymatty.NPCNavigator.Graph;

import com.mattymatty.NPCNavigator.Graph.Movements.EastMovement;
import com.mattymatty.NPCNavigator.Graph.Movements.NorthMovement;
import com.mattymatty.NPCNavigator.Graph.Movements.SouthMovement;
import com.mattymatty.NPCNavigator.Graph.Movements.WestMovement;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Door;

import java.util.Collections;
import java.util.HashSet;
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
        BlockCell cell = this;
        inMovements = new HashSet<Movement>() {{
            add(Movement.checkCache(new EastMovement(cell, true)));
            add(Movement.checkCache(new WestMovement(cell, true)));
            add(Movement.checkCache(new SouthMovement(cell, true)));
            add(Movement.checkCache(new NorthMovement(cell, true)));
        }};
        outMovements = new HashSet<Movement>() {{
            add(Movement.checkCache(new EastMovement(cell, false)));
            add(Movement.checkCache(new WestMovement(cell, false)));
            add(Movement.checkCache(new SouthMovement(cell, false)));
            add(Movement.checkCache(new NorthMovement(cell, false)));
        }};
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
    public void reset() {
        this.affectedBlocks = Collections.emptySet();
        this.valid = true;
    }

    @Override
    public boolean update(int dept, Set<Updatable> visited,boolean silent) {
        if (visited.contains(this))
            return false;
        visited.add(this);
        boolean updated;
        Block top = pos.clone().add(0, 1, 0).getBlock();
        Block bottom = pos.getBlock();
        Block ground = pos.clone().add(0, -1, 0).getBlock();

        affectedBlocks = new HashSet<Block>(){{
            add(top);
            add(bottom);
            add(ground);
        }};

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

        boolean in = inMovements.stream().map((m) -> m.update(dept, visited,silent)).reduce(false,(a,b)->a || b);
        boolean out = outMovements.stream().map((m) -> m.update(dept, visited,silent)).reduce(false,(a,b)->a || b);
        updated |= in | out;
        return updated;
    }

    @Override
    public BlockCell clone() {
        return new BlockCell(this);
    }
}
