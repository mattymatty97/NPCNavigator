package com.mattymatty.NPCNavigator.Graph;

import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;

public abstract class Cell  implements Cloneable,Updatable{

    public static Object getLock(){
        throw new RuntimeException("Cell isn't implemented");
    }

    public static Cell getCell(Location loc){
        throw new RuntimeException("Cell isn't implemented");
    };

    public abstract Location getLocation();

    public abstract boolean isValid();

    public abstract Set<Movement> getInMovements();

    public abstract Set<Movement> getOutMovements();

    public boolean update(){
        return update(0, new HashSet<>());
    }
    public abstract boolean update(int dept, Set<Updatable> visited);

    public abstract Cell clone();

}
