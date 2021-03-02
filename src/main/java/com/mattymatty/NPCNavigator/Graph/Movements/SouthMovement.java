package com.mattymatty.NPCNavigator.Graph.Movements;

import com.mattymatty.NPCNavigator.Graph.Cell;
import com.mattymatty.NPCNavigator.Graph.Movement;
import org.bukkit.util.Vector;

public class SouthMovement extends StraightMovement{

    private static final Vector vector = new Vector(0,0,1);

    public SouthMovement(Cell destination, boolean direction) {
        super(destination,direction);
    }

    public SouthMovement(SouthMovement toClone) {
        super(toClone);
    }

    @Override
    public Vector getVector() {
        return vector.clone();
    }

    @Override
    public Movement clone() {
        return new SouthMovement(this);
    }
}
