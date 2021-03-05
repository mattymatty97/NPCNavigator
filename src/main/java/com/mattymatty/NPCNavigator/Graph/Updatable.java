package com.mattymatty.NPCNavigator.Graph;

import java.util.Set;

public interface Updatable {

    void reset();

    boolean update();

    boolean update(int dept, Set<Updatable> visited);

    boolean update(int dept, Set<Updatable> visited,boolean silent);
}
