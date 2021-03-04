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

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("Duplicates")
public class Commands implements CommandExecutor {

    private Location pos1;
    private Location pos2;
    private final List<BukkitTask> particles = new LinkedList<>();

    Commands() {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
        } else {
            Player player = (Player) sender;
            if (args.length == 1) {
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
                        NPCNavigator.instance.approxCost = !NPCNavigator.instance.approxCost;
                        sender.sendMessage("Approximation of distance is now " + ((NPCNavigator.instance.approxCost) ? "Active" : "Inactive"));
                        return true;
                    }
                    case "path": {
                        long stime = System.currentTimeMillis();

                        sender.sendMessage("Starting path");
                        DStarLitePathfinder pathfinder = DStarLitePathfinder.newDefaultPathfinder();

                        pathfinder.init(pos1.toBlockLocation(), pos2.toBlockLocation()).start();
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
                                    }
                                } else {
                                    sender.sendMessage("No Path Found");
                                    this.cancel();
                                }
                            }
                        }.runTaskTimer(NPCNavigator.instance,5,5);
                        particles.add(particle);
                        /* particle = Bukkit.getServer().getScheduler().runTaskTimer(NPCNavigator.instance, () -> {
                            int i = 0;
                            for (Location loc : path) {
                                i++;
                                Location act = cloneLoc(loc);
                                Bukkit.getServer().getScheduler().runTaskLater(NPCNavigator.instance, () -> {
                                    Objects.requireNonNull(act.getWorld()).spawnParticle(Particle.VILLAGER_HAPPY, act, 7);
                                    }, i * 10L);
                                }
                        }, 15, 140);
                        particles.add(particle);*/

                        return true;
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
