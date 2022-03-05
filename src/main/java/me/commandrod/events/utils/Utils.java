package me.commandrod.events.utils;

import me.commandrod.events.api.event.Event;
import org.bukkit.ChatColor;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;

import java.util.List;

public class Utils {

    public static String color(String s){ return ChatColor.translateAlternateColorCodes('&', s); }

    public static void heal(Player player, boolean clear){
        if (clear) player.getInventory().clear();
        player.setHealth(20);
        player.setFoodLevel(20);
        player.getActivePotionEffects().forEach(potion -> player.removePotionEffect(potion.getType()));
    }

    public static List<String> colorList(List<String> list) {
        for (int i = 0; i < list.size(); i++){
            list.set(i, Utils.color(list.get(i)));
        }
        return list;
    }

    public static void border(Event event, float minutes, int dmg) {
        WorldBorder border = event.getSpawnLocation().getWorld().getWorldBorder();
        border.setSize(200);
        border.setSize(10, (long) (minutes*60));
        border.setWarningTime(15);
        border.setWarningDistance(0);
        border.setDamageBuffer(0);
        border.setDamageAmount(dmg);
        border.setCenter(event.getSpawnLocation());
    }

    public static void border(Event event, float minutes) {
        border(event, minutes, 1);
    }
}