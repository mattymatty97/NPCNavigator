package com.mattymatty.NPCNavigator.Graph.Movements;

import com.mattymatty.NPCNavigator.Graph.Cell;
import com.mattymatty.NPCNavigator.Graph.Movement;
import com.mattymatty.NPCNavigator.Graph.Updatable;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Door;

import java.util.Collections;
import java.util.HashSet;
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
        this.oldCost = toClone.oldCost;
    }

    @Override
    public void reset() {
        oldCost = cost;
        boolean updated = !valid;
        affectedBlocks = Collections.emptySet();
        valid = true;
        cost = getVector().length();
        if(updated)
            emit(this);
    }

    @Override
    public boolean update(){
        return update(0,new HashSet<>());
    }

    @Override
    public boolean update(int dept, Set<Updatable> visited){
        return update(dept,visited,false);
    }

    @Override
    public boolean update(int dept, Set<Updatable> visited,boolean silent){
        if(visited.contains(this))
            return false;
        visited.add(this);
        double oldCost = this.cost;
        boolean updated;
        boolean propagate_update = false;

        Cell originCell = (silent)?Cell.getCellSilently(this.origin):Cell.getCell(this.origin);
        Cell destCell = (silent)?Cell.getCellSilently(this.dest):Cell.getCell(this.dest);

        if(dept>0){
            if(originCell!=null)
                propagate_update = originCell.update(dept - 1,visited);
            if(destCell!=null) {
                propagate_update |= destCell.update(dept - 1, visited);
            }
        }

    if(( originCell == null || originCell.isValid()) && (destCell == null || destCell.isValid())){
            Location loc = dest;
            Block top = loc.clone().add(0, 1, 0).getBlock();
            Block bottom = loc.getBlock();
            Block ground = loc.clone().add(0, -1, 0).getBlock();

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
        if(updated){
            this.oldCost = oldCost;
            emit(this);
        }
        return updated | propagate_update;
    }

    double oldCost = Double.POSITIVE_INFINITY;
    @Override
    public double getOldCost() {
        return oldCost;
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
