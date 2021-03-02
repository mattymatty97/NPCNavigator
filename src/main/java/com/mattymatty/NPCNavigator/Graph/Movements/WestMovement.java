package com.mattymatty.NPCNavigator.Graph.Movements;

import com.mattymatty.NPCNavigator.Graph.Cell;
import com.mattymatty.NPCNavigator.Graph.Movement;
import org.bukkit.util.Vector;

public class WestMovement extends StraightMovement{

    private static final Vector vector = new Vector(-1,0,0);

    public WestMovement(Cell destination, boolean direction) {
        super(destination,direction);
    }

    public WestMovement(WestMovement toClone) {
        super(toClone);
    }

    @Override
    public Vector getVector() {
        return vector.clone();
    }

    @Override
    public Movement clone() {
        return new WestMovement(this);
    }
}
