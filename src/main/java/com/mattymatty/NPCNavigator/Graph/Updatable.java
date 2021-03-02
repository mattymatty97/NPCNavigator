package com.mattymatty.NPCNavigator.Graph;

import java.util.Set;

public interface Updatable {
    public boolean update();

    public boolean update(int dept, Set<Updatable> visited);
}
