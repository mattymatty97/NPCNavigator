package com.mattymatty.NPCNavigator.DStarLite;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class State implements java.io.Serializable
{
    private Location location;
    public Key k = new Key(Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY);

    //Default constructor
    public State()
    {
        location = new Location(null,0,0,0);
    }

    public State(Location loc)
    {
        location = loc.clone();
    }

    //Overloaded constructor
    public State(Location loc, Key k)
    {
        location = loc.clone();
        this.k = new Key(k.left, k.right);
    }


    //Overloaded constructor
    public State(State other)
    {
        location = other.getLocation();
        this.k = new Key(k);
    }

    public State setLocation(Location location) {
        this.location = location.clone();
        return this;
    }

    public State setX(double x){
        location.setX(x);
        return this;
    }
    public State setY(double y){
        location.setY(y);
        return this;
    }
    public State setZ(double z){
        location.setZ(z);
        return this;
    }

    public Vector getVector() {
        return location.toVector();
    }
    public Location getLocation() {
        return location.clone();
    }

    //Equals
    public boolean eq(State s2)
    {
        return location.equals(s2.getLocation());
    }

    //Not Equals
    public boolean neq(State s2)
    {
        return !eq(s2);
    }



    //Use Bukkit Vector hash code Function
    @Override
    public int hashCode()
    {
        return location.hashCode();
    }

    @Override public boolean equals(Object aThat) {
        //check for self-comparison
        if ( this == aThat ) return true;

        if ( !(aThat instanceof State) ) return false;;

        //cast to native object is now safe
        State that = (State)aThat;

        return eq(that);

    }

}
