package com.mattymatty.NPCNavigator.DStarLite;

public class State implements Comparable, java.io.Serializable
{
    public int x=0;
    public int y=0;
    public int z=0;
    public Pair<Double, Double> k = new Pair<>(Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY);


    //Default constructor
    public State()
    {

    }

    //Overloaded constructor
    public State(int x, int y,int z, Pair<Double,Double> k)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.k = k;
    }

    //Overloaded constructor
    public State(int x, int y,int z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    //Overloaded constructor
    public State(State other)
    {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
        this.k = other.k;
    }

    //Equals
    public boolean eq(State s2)
    {
        return ((this.x == s2.x) && (this.y == s2.y) && (this.z == s2.z) );
    }

    //Not Equals
    public boolean neq(State s2)
    {
        return ((this.x != s2.x) || (this.y != s2.y)  || (this.z != s2.z));
    }

    //Greater than
    public boolean gt( State s2)
    {
        if (k.first()-0.00001 > s2.k.first()) return true;
        else if (k.first() < s2.k.first()-0.00001) return false;
        return k.second() > s2.k.second();
    }

    //Less than or equal to
    public boolean lte(State s2)
    {
        if (k.first() < s2.k.first()) return true;
        else if (k.first() > s2.k.first()) return false;
        return k.second() < s2.k.second() + 0.00001;
    }

    //Less than
    public boolean lt(State s2)
    {
        if (k.first() + 0.000001 < s2.k.first()) return true;
        else if (k.first() - 0.000001 > s2.k.first()) return false;
        return k.second() < s2.k.second();
    }

    //CompareTo Method. This is necessary when this class is used in a priority queue
    public int compareTo(Object that)
    {
        //This is a modified version of the gt method
        State other = (State)that;
        if (k.first()-0.00001 > other.k.first()) return 1;
        else if (k.first() < other.k.first()-0.00001) return -1;
        if (k.second() > other.k.second()) return 1;
        else if (k.second() < other.k.second()) return -1;
        return 0;
    }

    //Use Bukkit Vector hash code Function
    @Override
    public int hashCode()
    {   int hash = 7;

        hash = 79 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
        hash = 79 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
        hash = 79 * hash + (int) (Double.doubleToLongBits(this.z) ^ (Double.doubleToLongBits(this.z) >>> 32));
        return hash;
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
