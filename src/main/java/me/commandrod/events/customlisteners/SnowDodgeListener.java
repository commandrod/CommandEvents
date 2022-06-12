package me.commandrod.events.customlisteners;

import me.commandrod.commandapi.items.ItemUtils;
import me.commandrod.commandapi.utils.SoundUtils;
import me.commandrod.events.Main;
import me.commandrod.events.api.event.EventManager;
import me.commandrod.events.api.event.EventType;
import me.commandrod.events.events.SnowDodge;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class SnowDodgeListener implements Listener {

    @EventHandler
    public void onHit(ProjectileHitEvent e) {
        if (!EventManager.isEventRunning()) return;
        if (!Main.getEvent().getType().equals(EventType.SNOWDODGE)) return;
        Entity en = e.getHitEntity();
        if (en == null) return;
        if (!(en instanceof Player p)) return;
        p.damage(2);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (!EventManager.isEventRunning()) return;
        if (!Main.getEvent().getType().equals(EventType.SNOWDODGE)) return;
        SnowDodge event = (SnowDodge) Main.getEvent();
        if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        e.setCancelled(true);
        Block clickedBlock = e.getClickedBlock();
        if (clickedBlock == null) return;
        if (!clickedBlock.getType().equals(SnowDodge.BLOCKTYPE)) return;
        Player p = e.getPlayer();
        boolean cooldown = event.cooldown(p, clickedBlock);
        if (cooldown) return;
        event.onClick(p, clickedBlock);
    }
}
