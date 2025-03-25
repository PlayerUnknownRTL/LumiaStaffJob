package me.lumia.staffjoblumia.commands;

import me.clip.placeholderapi.PlaceholderAPI;
import me.lumia.staffjoblumia.StaffJobLumia;
import me.lumia.staffjoblumia.colors.RGBcolor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class MainCommands implements CommandExecutor, TabCompleter {

    FileConfiguration c = StaffJobLumia.getPlugin().getConfig();
    List<Player> playerJob = new ArrayList<>();

    HashMap<UUID, Long> cooldown = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if(!(commandSender instanceof Player)) {
            commandSender.sendMessage(c.getString("Errors.consoleError"), PlaceholderAPI.setPlaceholders((OfflinePlayer) commandSender, s));
            return true;
        }

        long n = System.currentTimeMillis();
        Player p = (Player) commandSender;
        UUID uuid = p.getUniqueId();

        if(strings.length < 1) {
            p.sendMessage(RGBcolor.RGBcolors.colorize(c.getString("Errors.lenError")), PlaceholderAPI.setPlaceholders(p, s));
            return true;
        }

        if(cooldown.containsKey(uuid)) {
            long l = cooldown.get(uuid);
            long t = n - l;

            if(t < c.getLong("Settings.cooldownPlugin") * 1000) {
                long lf = (c.getLong("Settings.cooldownPlugin") * 1000 - t) / 1000;
                String cdM = String.format(RGBcolor.RGBcolors.colorize(c.getString("Errors.colldownError")), lf);
                p.sendMessage(cdM);
                return true;
            }
        }

        cooldown.put(uuid, n);

        if(strings[0].equalsIgnoreCase("start")) {

            startMessage(p);

        } else if(strings[0].equalsIgnoreCase("end")) {

            endMessage(p);

        } else if(strings[0].equalsIgnoreCase("staff")) {

            staffList(p);

        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Player p = (Player) commandSender;
        List<String> tab = new ArrayList<>();
        List<String> keys = new ArrayList<>(c.getConfigurationSection("Permissions").getKeys(false));

        for (String key : keys) {
            String permission = c.getString("Permissions." + key + ".permission");

            if (strings.length == 1) {
                if (p.hasPermission(permission)) {
                    tab.add("start");
                    tab.add("staff");
                    tab.add("end");
                } else {
                    tab.add("staff");
                }
            }
        }

        return tab;
    }

    private void startMessage(Player p) {

        String placeholder = "";

        List<String> keys = new ArrayList<>(c.getConfigurationSection("Permissions").getKeys(false));
        boolean hasPerm = false;

        for(String s : keys) {
            List<String> start = c.getStringList("Permissions." + s + ".startJob");
            List<String> cmdCfg = c.getStringList("Permissions." + s + ".commandStart");
            String perm = c.getString("Permissions." + s + ".permission");

            if (p.hasPermission(perm)) {
                hasPerm = true;
            }

            if(!hasPerm) {
                p.sendMessage(RGBcolor.RGBcolors.colorize(c.getString("Errors.errorNoPermission")), PlaceholderAPI.setPlaceholders(p, placeholder));
                return;
            }

            if(playerJob.contains(p)) {
                p.sendMessage(RGBcolor.RGBcolors.colorize(c.getString("Errors.errorStart")), PlaceholderAPI.setPlaceholders(p, placeholder));
                return;
            }

            for(String msg : start) {
                msg = PlaceholderAPI.setPlaceholders(p, msg);
                Bukkit.broadcastMessage(RGBcolor.RGBcolors.colorize(msg));
            }

            if(!cmdCfg.isEmpty()) {
                for(String cmd : cmdCfg) {
                    Bukkit.dispatchCommand(p, cmd);
                }
            }

            if(c.getBoolean("Settings.playSoundMain")) {
                if(c.getBoolean("Permissions." + s + ".soundPlay")) {
                    for(Player pOnline : Bukkit.getOnlinePlayers()) {
                        if(c.getString("Permissions." + s + ".soundStart") != null && !c.getString("Permissions." + s + ".soundStart").isEmpty()) {
                            Sound sound = Sound.valueOf(c.getString("Permissions." + s + ".soundStart"));
                            pOnline.playSound(p.getLocation(), sound, 1f, 1f);
                        }
                    }
                }
            }

            playerJob.add(p);
            return;
        }
    }

    private void endMessage(Player p) {

        String placeholder = "";

        List<String> keys = new ArrayList<>(c.getConfigurationSection("Permissions").getKeys(false));
        Boolean hasPerm = false;

        for(String s : keys) {
            List<String> end = new ArrayList<>(c.getStringList("Permissions." + s + ".endJob"));

            String perm = c.getString("Permissions." + s + ".permission");

            if (p.hasPermission(perm)) {
                hasPerm = true;
            }

            if(!hasPerm) {
                p.sendMessage(RGBcolor.RGBcolors.colorize(c.getString("Errors.errorNoPermission")), PlaceholderAPI.setPlaceholders(p, placeholder));
                return;
            }

            if(!playerJob.contains(p)) {
                p.sendMessage(RGBcolor.RGBcolors.colorize(c.getString("Errors.errorEnd")), PlaceholderAPI.setPlaceholders(p, placeholder));
                return;
            }

            for(String msg : end) {
                msg = PlaceholderAPI.setPlaceholders(p, msg);
                Bukkit.broadcastMessage(RGBcolor.RGBcolors.colorize(msg));
            }

            playerJob.remove(p);

            if(c.getBoolean("Settings.playSoundMain")) {
                if(c.getBoolean("Permissions." + s + ".soundPlay")) {
                    for(Player pOnline : Bukkit.getOnlinePlayers()) {
                        if(c.getString("Permissions." + s + ".soundEnd") != null && !c.getString("Permissions." + s + ".soundEnd").isEmpty()) {
                            Sound sound = Sound.valueOf(c.getString("Permissions." + s + ".soundEnd"));
                            pOnline.playSound(p.getLocation(), sound, 1f, 1f);
                        }
                    }
                }
            }
            return;
        }
    }

    private void staffList(Player p) {

        String placeholder = "";

        int top = 1;

        List<String> keys = new ArrayList<>(c.getConfigurationSection("Permissions").getKeys(false));

        for(String s : keys) {
            List<String> staffList = new ArrayList<>();
            String staff = c.getString("Permissions." + s + ".permission");

            for (Player pOnline : Bukkit.getOnlinePlayers()) {
                if(pOnline.hasPermission(staff)) {
                    staffList.add(pOnline.getName());
                }
            }

            if(staff.isEmpty()) {
                p.sendMessage(RGBcolor.RGBcolors.colorize(c.getString("Errors.listNull")));
                return;
            }

            for(String msg : staffList) {
                p.sendMessage(RGBcolor.RGBcolors.colorize("&7" + top + " | " + msg), PlaceholderAPI.setPlaceholders(p, placeholder));
                top++;
            }
        }
    }
}
