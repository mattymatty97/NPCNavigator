package com.mattymatty.NPCNavigator;

import com.mattymatty.NPCNavigator.DStarLite.DStarLitePathfinder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@SuppressWarnings("Duplicates")
public class Commands implements CommandExecutor {

    private Location pos1;
    private Location pos2;
    private final List<BukkitTask> particles = new LinkedList<>();

    Commands() {
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
        } else {
            Player player = (Player) sender;
            if (args.length > 0) {
                switch (args[0].toLowerCase()) {
                    case "pos1": {
                        pos1 = player.getLocation();
                        pos1.setY(Math.ceil(pos1.getY()));
                        sender.sendMessage("Pos1 set");
                        return true;
                    }
                    case "pos2": {
                        pos2 = player.getLocation();
                        pos2.setY(Math.ceil(pos2.getY()));
                        sender.sendMessage("Pos2 set");
                        return true;
                    }
                    case "particle": {
                        particles.forEach(BukkitTask::cancel);
                        sender.sendMessage("Particles cleared");
                        return true;
                    }
                    case "approx": {
                        NPCNavigator.approxCost = !NPCNavigator.approxCost;
                        sender.sendMessage("Approximation of distance is now " + ((NPCNavigator.approxCost) ? "Active" : "Inactive"));
                        return true;
                    }
                    case "path": {
                        int range = 0;
                        try{
                            if(args.length>1)
                                range = Integer.parseInt(args[1]);
                        }catch (NumberFormatException ignored){}

                        sender.sendMessage("Starting path");
                        DStarLitePathfinder pathfinder = DStarLitePathfinder.newDefaultPathfinder();

                        pathfinder.init(pos1.toBlockLocation(), pos2.toBlockLocation()).preload(range).start();
                        LinkedList<Location> path = new LinkedList<Location>();
                        path.add(pos1.toCenterLocation());
                        BukkitTask particle;
                        particle = new BukkitRunnable() {
                            @Override
                            public void run() {
                                Location next = pathfinder.next();
                                if (next != null) {
                                    if (!next.toCenterLocation().equals(path.peekLast())) {
                                        Location cur = next.toCenterLocation();
                                        path.add(cur);
                                        Objects.requireNonNull(cur.getWorld()).spawnParticle(Particle.VILLAGER_HAPPY, cur, 7);
                                    }else{
                                        if(pathfinder.getStatus() == DStarLitePathfinder.Status.Done){
                                            sender.sendMessage("Destination reached");
                                            this.cancel();
                                        }else if (pathfinder.getStatus() == DStarLitePathfinder.Status.Failed){
                                            sender.sendMessage("No Path Found");
                                            this.cancel();
                                        }
                                    }
                                } else {
                                    sender.sendMessage("No Path Found");
                                    this.cancel();
                                }
                            }
                        }.runTaskTimer(NPCNavigator.instance,5,5);
                        particles.add(particle);
                        return true;
                    }
                    case "debug": {
                       if(args.length > 1){
                           try {
                               NPCNavigator.DebugLevel level = NPCNavigator.DebugLevel.valueOf(args[1]);
                               NPCNavigator.debugLevel = level;
                               sender.sendMessage("Debug level set to " + level);
                               return true;
                           }catch (NumberFormatException ignored){}
                       }
                       sender.sendMessage("Available levels: " + Arrays.stream(NPCNavigator.DebugLevel.values()).map(Objects::toString).collect(Collectors.joining(",")));
                    }
                }
            }
        }
        return false;
    }

    private void showParticles(List<Location> locations) {
        showParticles(locations, false);
    }

    private void showParticles(List<Location> locations, boolean isPath) {
        showParticles(locations, isPath, Particle.VILLAGER_HAPPY);
    }

    private void showParticles(List<Location> locations, Particle type) {
        showParticles(locations, false, type);
    }

    private void showParticles(List<Location> locations, boolean isPath, Particle type) {
        BukkitTask particle = Bukkit.getServer().getScheduler().runTaskTimer(NPCNavigator.instance, () -> {
            int i = 0;
            for (Location loc : locations) {
                i++;
                Location act = cloneLoc(loc);
                if (!isPath || i == 0) {
                    Objects.requireNonNull(act.getWorld()).spawnParticle(type, cloneLoc(act).add(0, 0.5, 0), 7);
                } else {
                    Bukkit.getServer().getScheduler().runTaskLater(NPCNavigator.instance, () -> {
                        Objects.requireNonNull(act.getWorld()).spawnParticle(type, cloneLoc(act).add(0, 0.5, 0), 7);
                    }, i * 10);
                }
            }
        }, 5, Math.max(10, Math.min(locations.size() * 10, 140)));
        particles.add(particle);
    }


    private Location cloneLoc(Location loc) {
        return new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());
    }

}
