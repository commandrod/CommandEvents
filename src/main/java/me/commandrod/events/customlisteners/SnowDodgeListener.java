package me.commandrod.events.customlisteners;

import me.commandrod.commandapi.items.ItemUtils;
import me.commandrod.events.Main;
import me.commandrod.events.api.event.EventManager;
import me.commandrod.events.api.event.EventType;
import me.commandrod.events.events.SnowDodge;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Optional;

public class SnowDodgeListener implements Listener {

    public Optional<SnowDodge> getEvent() {
        if (!EventManager.isEventRunning()) return Optional.empty();
        if (!Main.getEvent().getType().equals(EventType.SNOWDODGE)) return Optional.empty();
        return Optional.ofNullable((SnowDodge) Main.getEvent());
    }

    @EventHandler
    public void onHit(ProjectileHitEvent e) {
        Optional<SnowDodge> opEvent = this.getEvent();
        if (opEvent.isEmpty()) return;
        SnowDodge event = opEvent.get();
        Entity en = e.getHitEntity();
        if (en == null) return;
        if (!(en instanceof Player p)) return;
        Projectile snowball = e.getEntity();
        if (!(snowball.getShooter() instanceof Player shooter)) return;
        event.damagePlayer(p, shooter);
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        ItemStack item = e.getItem().getItemStack();
        Optional<SnowDodge> opEvent = this.getEvent();
        if (opEvent.isEmpty()) return;
        SnowDodge.PowerUpManager manager = SnowDodge.PowerUpManager.from(p);
        String ability = ItemUtils.getIdFromItem(item).replace("SNOWDODGE_", "");
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(),
                () -> p.getInventory().clear(p.getInventory().first(item)), 2);
        if (!ability.startsWith("EXTRA")) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 200, 1, false, false));
            return;
        }
        SnowDodge.PowerUp powerUp = SnowDodge.PowerUp.valueOf(ability);
        manager.activePowerUps().add(powerUp);
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(),
                () -> manager.activePowerUps().remove(powerUp), 300);
    }
}
