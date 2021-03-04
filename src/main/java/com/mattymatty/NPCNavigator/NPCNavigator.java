package com.mattymatty.NPCNavigator;

import com.destroystokyo.paper.event.block.TNTPrimeEvent;
import com.mattymatty.NPCNavigator.Graph.Cell;
import com.mattymatty.NPCNavigator.Graph.Updatable;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class NPCNavigator extends JavaPlugin {

    public static NPCNavigator instance;

    public boolean approxCost = false;

    @Override
    public void onLoad() {
        super.onLoad();
        instance = this;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        Objects.requireNonNull(this.getCommand("npcnavigator")).setExecutor(new Commands());
        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.LOWEST)
            public void onBlockEvent(BlockBreakEvent event){
                runNextTick(()->{
                    if(!event.isCancelled())
                        onBlockChange(event.getBlock());
                });
            }

            @EventHandler(priority = EventPriority.LOWEST)
            public void onBlockEvent(BlockPlaceEvent event){
                runNextTick(()->{
                    if(!event.isCancelled())
                        onBlockChange(event.getBlock());
                });
            }

            @EventHandler(priority = EventPriority.LOWEST)
            public void onBlockEvent(EntityChangeBlockEvent event){
                runNextTick(()->{
                    if(!event.isCancelled())
                        onBlockChange(event.getBlock());
                });
            }

            @EventHandler(priority = EventPriority.LOWEST)
            public void onBlockEvent(BlockFadeEvent event){
                runNextTick(()->{
                    if(!event.isCancelled())
                        onBlockChange(event.getBlock());
                });
            }

            @EventHandler(priority = EventPriority.LOWEST)
            public void onBlockEvent(BlockFormEvent event){
                runNextTick(()->{
                    if(!event.isCancelled())
                        onBlockChange(event.getBlock());
                });
            }

            @EventHandler(priority = EventPriority.LOWEST)
            public void onBlockEvent(BlockIgniteEvent event){
                runNextTick(()->{
                    if(!event.isCancelled())
                        onBlockChange(event.getBlock());
                });
            }

            @EventHandler(priority = EventPriority.LOWEST)
            public void onBlockEvent(BlockFertilizeEvent event){
                runNextTick(()->{
                    if(!event.isCancelled())
                        onBlockChange(event.getBlock());
                });
            }

            @EventHandler(priority = EventPriority.LOWEST)
            public void onBlockEvent(FluidLevelChangeEvent event){
                runNextTick(()->{
                    if(!event.isCancelled())
                        onBlockChange(event.getBlock());
                });
            }

            @EventHandler(priority = EventPriority.LOWEST)
            public void onBlockEvent(LeavesDecayEvent event){
                runNextTick(()->{
                    if(!event.isCancelled())
                        onBlockChange(event.getBlock());
                });
            }

            @EventHandler(priority = EventPriority.LOWEST)
            public void onBlockEvent(BlockBurnEvent event){
                    runNextTick(()->{
                        if(!event.isCancelled())
                        onBlockChange(event.getBlock());
                    });
            }

            @EventHandler(priority = EventPriority.LOWEST)
            public void onBlockEvent(BlockMultiPlaceEvent event){
                runNextTick(()-> {
                    if(event.isCancelled())
                        return;
                    Set<Updatable> visited = new HashSet<>();
                    for (BlockState state : event.getReplacedBlockStates())
                        onBlockChange(state.getBlock(), visited);
                });
            }

            @EventHandler(priority = EventPriority.LOWEST)
            public void onBlockEvent(BlockExplodeEvent event){
                runNextTick(()-> {
                    if (event.isCancelled())
                        return;
                    Set<Updatable> visited = new HashSet<>();
                    for (Block block : event.blockList())
                        onBlockChange(block, visited);
                });
            }

            @EventHandler(priority = EventPriority.LOWEST)
            public void onBlockEvent(EntityExplodeEvent event){
                runNextTick(()-> {
                    if (event.isCancelled())
                        return;
                    Set<Updatable> visited = new HashSet<>();
                    for (Block block : event.blockList())
                        onBlockChange(block, visited);
                });
            }

            private void runNextTick(Runnable runnable){
                getServer().getScheduler().runTaskLater(instance,runnable,3);
            }

            private void onBlockChange(Block block) {
                onBlockChange(block,new HashSet<>());
            }

            private void onBlockChange(Block block, Set<Updatable> visited) {
                Cell cell = Cell.getCellSilently(block.getLocation());
                if(cell!=null)
                    cell.update(1,visited);
            }
        },this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
