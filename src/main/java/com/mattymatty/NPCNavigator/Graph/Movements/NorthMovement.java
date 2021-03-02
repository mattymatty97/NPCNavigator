package com.mattymatty.NPCNavigator.Graph.Movements;

import com.mattymatty.NPCNavigator.Graph.Cell;
import com.mattymatty.NPCNavigator.Graph.Movement;
import org.bukkit.util.Vector;

public class NorthMovement extends StraightMovement{

    private static final Vector vector = new Vector(0,0,-1);

    public NorthMovement(Cell destination, boolean direction) {
        super(destination,direction);
    }

    public NorthMovement(NorthMovement toClone) {
        super(toClone);
    }

    @Override
    public Vector getVector() {
        return vector.clone();
    }

    @Override
    public Movement clone() {
        return new NorthMovement(this);
    }
}
