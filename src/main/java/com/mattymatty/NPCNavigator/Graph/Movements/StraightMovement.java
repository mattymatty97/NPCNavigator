package com.mattymatty.NPCNavigator.Graph.Movements;

import com.mattymatty.NPCNavigator.Graph.Cell;
import com.mattymatty.NPCNavigator.Graph.Movement;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Door;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Set;

public abstract class StraightMovement extends Movement {
    private final boolean direction;
    private final Location origin;
    private final Location dest;
    private Set<Block> affectedBlocks;
    private double cost;
    private boolean valid;

    public StraightMovement(Cell cell, boolean destination) {
        direction = destination;
        if(destination) {
            dest = cell.getLocation();
            origin = dest.clone().subtract(getVector());
        }else{
            origin = cell.getLocation();
            dest = origin.clone().add(getVector());
        }
        affectedBlocks = Collections.emptySet();
        valid = true;
        cost = getVector().length();
    }

    protected StraightMovement(StraightMovement toClone){
        this.direction = toClone.direction;
        this.origin = toClone.origin.clone();
        this.dest = toClone.dest.clone();
        this.affectedBlocks = Collections.unmodifiableSet(toClone.affectedBlocks);
        this.cost = toClone.cost;
        this.valid = toClone.valid;
    }

    public boolean update(){
        return update(true);
    }

    public boolean update(boolean propagate){
        saveCost();
        boolean updated;
        Cell origCell = Cell.getCell(origin);
        Cell destCell = Cell.getCell(dest);
        if(origCell != null && origCell.isValid() && destCell != null && destCell.isValid()) {
            Location loc = dest;
            Block top = loc.clone().add(0, 1, 0).getBlock();
            Block bottom = loc.getBlock();
            Block ground = loc.clone().add(0, -1, 0).getBlock();

            affectedBlocks = Set.of(top, bottom, ground);

            if (
                    (top.isPassable() || top.getType().data == Door.class) &&
                            (bottom.isPassable() || bottom.getType().data == Door.class) &&
                            !ground.isPassable()
            ) {
                updated = !valid;
                valid = true;
                cost = getVector().length();
            } else {
                updated = valid;
                valid = false;
                cost = Double.POSITIVE_INFINITY;
            }
        }else{
            updated = valid;
            affectedBlocks = Collections.emptySet();
            valid = false;
            cost = Double.POSITIVE_INFINITY;
        }
        if (updated && propagate) {
            Cell.getCell(origin).update(false);
            Cell.getCell(dest).update(false);
        }
        return updated;
    }

    @Override
    public Location getOrigin() {
        return origin.clone();
    }

    @Override
    public Location getDest() {
        return dest.clone();
    }

    @Override
    public double getCost() {
        return cost;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public Set<Block> getAffectedBlocks() {
        return Collections.unmodifiableSet(affectedBlocks);
    }

}
