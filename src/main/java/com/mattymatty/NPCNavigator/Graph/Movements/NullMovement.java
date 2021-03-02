package com.mattymatty.NPCNavigator.Graph.Movements;

import com.mattymatty.NPCNavigator.Graph.Cell;
import com.mattymatty.NPCNavigator.Graph.Movement;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Set;

public class NullMovement extends StraightMovement {

    private static final Vector vector = new Vector(0, 0, 0);

    private final Location origin;
    private final Location dest;
    private final WeakReference<Cell> cell;
    private double cost;
    private boolean valid;


    public NullMovement(Cell cell) {
        super(cell,false);
        this.cell = new WeakReference<Cell>(cell);
        dest = cell.getLocation();
        origin = cell.getLocation();
        valid = true;
        cost = 0;
    }

    public NullMovement(NullMovement toClone) {
        super(toClone);
        this.origin = toClone.origin.clone();
        this.dest = toClone.origin.clone();
        this.valid = true;
        this.cost = 0;
        this.cell = new WeakReference<>(toClone.cell.get());
    }

    public boolean update() {
        return update(true);
    }

    public boolean update(boolean propagate) {
        return false;
    }

    @Override
    public double getOldCost() {
        return 0;
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
        return Collections.emptySet();
    }

    @Override
    public Vector getVector() {
        return vector.clone();
    }

    @Override
    public Movement clone() {
        return new NullMovement(this);
    }
}
