package com.mattymatty.NPCNavigator.DStarLite;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class Key extends MutablePair<Double, Double> {
    /**
     * Create a new pair instance.
     *
     * @param left  the left value, may be null
     * @param right the right value, may be null
     */
    public Key(Double left, Double right) {
        super(left, right);
    }

    public Key(Key other) {
        super(other.left, other.right);
    }

    //Greater than
    public boolean gt(Key s2)
    {
        if (getLeft() > s2.getLeft()) return true;
        if (getLeft() < s2.getLeft()) return false;
        return getRight() > s2.getRight();
    }

    //Less than or equal to
    public boolean lte(Key  s2)
    {
        if (getLeft() < s2.getLeft()) return true;
        if (getLeft() > s2.getLeft()) return false;
        return getRight() <= s2.getRight();
    }

    //Less than
    public boolean lt(Key  s2)
    {
        if (getLeft() < s2.getLeft()) return true;
        if (getLeft() > s2.getLeft()) return false;
        return getRight() < s2.getRight();
    }

    @Override
    //CompareTo Method. This is necessary when this class is used in a priority queue
    public int compareTo(Pair<Double,Double> other)
    {
        //This is a modified version of the gt method
        if (getLeft() > other.getLeft()) return 1;
        if (getLeft() < other.getLeft()) return -1;
        if (getRight() > other.getRight()) return 1;
        if (getRight() < other.getRight()) return -1;
        return 0;
    }
}
