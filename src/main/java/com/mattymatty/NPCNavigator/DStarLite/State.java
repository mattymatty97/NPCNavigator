package com.mattymatty.NPCNavigator.DStarLite;

import com.mattymatty.NPCNavigator.Utils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class State implements Comparable<State>, java.io.Serializable
{
    private Location location;
    public MutablePair<Double, Double> k = new MutablePair<>(Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY);


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
    public State(Location loc, MutablePair<Double,Double> k)
    {
        location = loc.clone();
        this.k = k;
    }


    //Overloaded constructor
    public State(State other)
    {
        location = other.getLocation();
        this.k = other.k;
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

    //Greater than
    public boolean gt( State s2)
    {
        if (k.getLeft()-0.00001 > s2.k.getLeft()) return true;
        else if (k.getLeft() < s2.k.getLeft()-0.00001) return false;
        return k.getRight() > s2.k.getRight();
    }

    //Less than or equal to
    public boolean lte(State s2)
    {
        if (k.getLeft() < s2.k.getLeft()) return true;
        else if (k.getLeft() > s2.k.getLeft()) return false;
        return k.getRight() < s2.k.getRight() + 0.00001;
    }

    //Less than
    public boolean lt(State s2)
    {
        if (k.getLeft() + 0.000001 < s2.k.getLeft()) return true;
        else if (k.getLeft() - 0.000001 > s2.k.getLeft()) return false;
        return k.getRight() < s2.k.getRight();
    }

    //CompareTo Method. This is necessary when this class is used in a priority queue
    public int compareTo(State other)
    {
        //This is a modified version of the gt method
        if (k.getLeft()-0.00001 > other.k.getLeft()) return 1;
        else if (k.getLeft() < other.k.getLeft()-0.00001) return -1;
        if (k.getRight() > other.k.getRight()) return 1;
        else if (k.getRight() < other.k.getRight()) return -1;
        return 0;
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
