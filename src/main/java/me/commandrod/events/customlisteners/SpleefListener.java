package me.commandrod.events.customlisteners;

import me.commandrod.events.Main;
import me.commandrod.events.api.event.EventManager;
import me.commandrod.events.api.event.EventType;
import me.commandrod.events.events.Spleef;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class SpleefListener implements Listener {

    @EventHandler
    public void onThrow(ProjectileLaunchEvent e) {
        if (!EventManager.isEventRunning()) return;
        if (!Main.getEvent().getType().equals(EventType.SPLEEF)) return;
        Spleef event = (Spleef) Main.getEvent();
        if (!(e.getEntity().getShooter() instanceof Player p)) return;
        event.getSnowballCounter().add(p);
    }

    @EventHandler
    public void onHit(ProjectileHitEvent e) {
        if (!EventManager.isEventRunning()) return;
        if (!Main.getEvent().getType().equals(EventType.SPLEEF)) return;
        Spleef event = (Spleef) Main.getEvent();
        Block block = e.getHitBlock();
        if (block == null) return;
        if (!block.getType().equals(Material.SNOW_BLOCK)) return;
        if (!(e.getEntity().getShooter() instanceof Player p)) return;
        block.setType(Material.AIR);
        event.getDestroyedBlocks().add(block);
        event.getBlocksCounter().add(p);
    }
}
