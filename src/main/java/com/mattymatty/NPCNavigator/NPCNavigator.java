package com.mattymatty.NPCNavigator;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

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
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
