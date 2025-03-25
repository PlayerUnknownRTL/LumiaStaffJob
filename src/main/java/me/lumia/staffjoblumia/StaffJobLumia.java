package me.lumia.staffjoblumia;

import me.lumia.staffjoblumia.commands.MainCommands;
import org.bukkit.plugin.java.JavaPlugin;

public final class StaffJobLumia extends JavaPlugin {

    static StaffJobLumia plugin;

    @Override
    public void onEnable() {

        saveDefaultConfig();
        getConfig().options().copyDefaults();
        plugin = this;

        getCommand("ljob").setExecutor(new MainCommands());
    }

    @Override
    public void onDisable() {

    }

    public static StaffJobLumia getPlugin() {
        return plugin;
    }
}
