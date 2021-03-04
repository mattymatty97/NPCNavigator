package com.mattymatty.NPCNavigator.Graph.Movements;

import com.mattymatty.NPCNavigator.Graph.Cell;
import com.mattymatty.NPCNavigator.Graph.Movement;
import com.mattymatty.NPCNavigator.Graph.Updatable;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class NullMovement extends Movement {

    private static final Vector vector = new Vector(0, 0, 0);

    private final Location origin;
    private final Location dest;
    private double cost;
    private boolean valid;


    public NullMovement(Cell cell) {
        dest = cell.getLocation();
        origin = cell.getLocation();
        valid = true;
        cost = 0;
    }

    public NullMovement(Location loc) {
        dest = loc;
        origin = loc;
        valid = true;
        cost = 0;
    }

    public NullMovement(NullMovement toClone) {
        this.origin = toClone.origin.clone();
        this.dest = toClone.origin.clone();
        this.valid = true;
        this.cost =  0;
    }

    @Override
    public boolean update() {
        return update(0, new HashSet<>());
    }

    @Override
    public boolean update(int dept, Set<Updatable> visited) {
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
