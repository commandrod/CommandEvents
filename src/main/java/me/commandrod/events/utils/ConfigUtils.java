package me.commandrod.events.utils;

import me.commandrod.events.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ConfigUtils {

    public static Location getLocation(String path) {
        FileConfiguration config = Main.getPlugin().getConfig();
        Location loc;
        World world;
        try {
            world = Bukkit.getWorld(config.getString(path + ".world"));
            loc = new Location(world, config.getDouble(path + ".x"), config.getDouble(path + ".y"), config.getDouble(path + ".z"), (float) config.getDouble(path + ".yaw"), (float) config.getDouble(path + ".pitch"));
        } catch (Exception ex){
            Bukkit.getLogger().log(Level.SEVERE, "There was a problem getting the spawn location for " + path + "!");
            return new Location(Bukkit.getWorld("world"), 0, 24, 0);
        }
        return loc;
    }

    public static void setLocation(Location location, String path) {
        FileConfiguration config = Main.getPlugin().getConfig();
        config.set(path + ".world", location.getWorld().getName());
        config.set(path + ".x", location.getX());
        config.set(path + ".y", location.getY());
        config.set(path + ".z", location.getZ());
        config.set(path + ".yaw", location.getYaw());
        config.set(path + ".pitch", location.getPitch());
        Main.getPlugin().saveConfig();
        Main.getPlugin().reloadConfig();
    }

    public static List<Block> getBlocksBetween(Location loc1, Location loc2) {
        List<Block> blocks = new ArrayList<>();
        int topBlockX = (Math.max(loc1.getBlockX(), loc2.getBlockX()));
        int bottomBlockX = (Math.min(loc1.getBlockX(), loc2.getBlockX()));

        int topBlockY = (Math.max(loc1.getBlockY(), loc2.getBlockY()));
        int bottomBlockY = (Math.min(loc1.getBlockY(), loc2.getBlockY()));

        int topBlockZ = (Math.max(loc1.getBlockZ(), loc2.getBlockZ()));
        int bottomBlockZ = (Math.min(loc1.getBlockZ(), loc2.getBlockZ()));

        for (int x = bottomBlockX; x <= topBlockX; x++) {
            for (int z = bottomBlockZ; z <= topBlockZ; z++) {
                for (int y = bottomBlockY; y <= topBlockY; y++) {
                    Block block = loc1.getWorld().getBlockAt(x, y, z);;
                    blocks.add(block);
                }
            }
        }
        return blocks;
    }
}