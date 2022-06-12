package me.commandrod.events.utils;

import me.commandrod.events.api.event.Event;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;

public class EventUtils {

    public static void heal(Player player, boolean clear){
        if (clear) player.getInventory().clear();
        player.setHealth(20);
        player.setFoodLevel(20);
        player.getActivePotionEffects().forEach(potion -> player.removePotionEffect(potion.getType()));
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

    public static String NULL = "&cNULL";

    public static String border(Event event) {
        int ogSize = (int) event.getSpawnLocation().getWorld().getWorldBorder().getSize();
        int size = ogSize > 10000 ? 200 : ogSize;
        return "&7בורדר: &b" + size;
    }
}