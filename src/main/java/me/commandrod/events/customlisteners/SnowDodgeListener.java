package me.commandrod.events.customlisteners;

import me.commandrod.events.Main;
import me.commandrod.events.api.event.EventManager;
import me.commandrod.events.api.event.EventType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

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
}
