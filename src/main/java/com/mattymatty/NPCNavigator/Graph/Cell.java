package com.mattymatty.NPCNavigator.Graph;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.bukkit.Location;

import java.awt.*;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public abstract class Cell {
    public static final LoadingCache<Location, Cell> cellCache = CacheBuilder.newBuilder().softValues()
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build(new CacheLoader<>() {
                @Override
                public BlockCell load(Location key) throws Exception {
                    return new BlockCell(key);
                }
            });

    public static Cell getCell(Location loc) {
        Cell cell = cellCache.getUnchecked(loc);
        return cell;
    }

    public static boolean update(Cell cell, int deept){
        LinkedHashSet<Cell> cells = new LinkedHashSet<>();
        cells.add(cell);
        Set<Movement> movements = new HashSet<>();
        int cellIndex = 0;
        for(int cur=0;cur<deept;cur++){
            int old_index = cellIndex;
            cellIndex = cells.size();
            cells.stream().skip(old_index).limit(old_index-cells.size()).forEach((c)->{
                c.getOutMovements().forEach(m->{
                    movements.add(m);
                    cells.add(Cell.getCell(m.getDest()));
                });
                c.getInMovements().forEach(m->{
                    movements.add(m);
                    cells.add(Cell.getCell(m.getOrigin()));
                });
            });
        }
        cells.forEach(c->c.update(false));
        return movements.stream().filter(m->m.update(false)).count() > 0;
    }

    public abstract Location getLocation();

    public abstract boolean isValid();

    public abstract Set<Movement> getInMovements();

    public abstract Set<Movement> getOutMovements();

    public boolean update(){
        return update(true);
    }

    public abstract boolean update(boolean propagate);

    public abstract Cell clone();

}
