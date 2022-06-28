package me.commandrod.events.utils;

import lombok.experimental.UtilityClass;
import me.commandrod.commandapi.utils.ConfigUtils;
import me.commandrod.events.api.event.Event;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WorldBorder;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@UtilityClass
public class EventUtils {

    public void heal(Player player, boolean clear) {
        if (clear) player.getInventory().clear();
        player.setHealth(20);
        player.setFoodLevel(20);
        player.getActivePotionEffects().forEach(potion -> player.removePotionEffect(potion.getType()));
    }

    public void border(Event event, float minutes, int dmg) {
        WorldBorder border = event.getSpawnLocation().getWorld().getWorldBorder();
        border.setSize(200);
        border.setSize(10, (long) (minutes*60));
        border.setWarningTime(15);
        border.setWarningDistance(0);
        border.setDamageBuffer(0);
        border.setDamageAmount(dmg);
        Optional<Location> opBorder = ConfigUtils.getInstance().getLocation(event.getConfigName() + "-border");
        opBorder.ifPresent(border::setCenter);
    }

    public void border(Event event, float minutes) {
        border(event, minutes, 1);
    }

    public String NULL = "&cNULL";

    public String border(Event event) {
        int ogSize = (int) event.getSpawnLocation().getWorld().getWorldBorder().getSize();
        int size = ogSize > 10000 ? 200 : ogSize;
        return "&7בורדר: &b" + size;
    }

    public int random(int max) {
        return ThreadLocalRandom.current().nextInt(max);
    }

    public boolean blockUnderIs(Player player, Material type) {
        return player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().equals(type);
    }
}