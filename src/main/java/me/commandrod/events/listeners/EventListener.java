package me.commandrod.events.listeners;

import me.commandrod.events.Main;
import me.commandrod.events.api.event.Event;
import me.commandrod.events.api.event.EventManager;
import me.commandrod.events.api.event.EventState;
import me.commandrod.events.api.event.Handle;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;

public class EventListener implements Listener {

    private void manageHandle(Cancellable event, Handle handle) {
        if (handle.equals(Handle.NONE)) return;
        boolean cancel = handle.equals(Handle.TRUE);
        event.setCancelled(cancel);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (!EventManager.isEventRunning()) return;
        Player p = e.getPlayer();
        Event event = Main.getEvent();
        if (event.isDead(p)) return;
        manageHandle(e, event.onInteract(p, e));
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!EventManager.isEventRunning()) return;
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (e.getClickedInventory() == null) return;
        if (!e.getClickedInventory().equals(p.getInventory())) return;
        if (e.getCurrentItem() == null) return;
        Event event = Main.getEvent();
        if (event.isDead(p)) {
            if (p.hasPermission("commandevents.bypass")) {
                e.setCancelled(false);
                return;
            }
            e.setCancelled(true);
            return;
        }
        Handle handle = event.onInventoryClick(p, e);
        manageHandle(e, handle);
    }

    @EventHandler
    public void onChange(PlayerChangedWorldEvent e) {
        if (!EventManager.isEventRunning()) return;
        Event event = Main.getEvent();
        Player p = e.getPlayer();
        if (!event.isDead(p)) return;
        p.teleport(event.getSpawnLocation());
        p.setGameMode(GameMode.SPECTATOR);
        event.eliminate(p);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCraft(CraftItemEvent e) {
        if (!EventManager.isEventRunning()) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLeave(PlayerQuitEvent e) {
        if (!EventManager.isEventRunning()) return;
        Player p = e.getPlayer();
        Event event = Main.getEvent();
        event.eliminate(p);
        event.end();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent e) {
        if (!EventManager.isEventRunning()) return;
        Player p = e.getPlayer();
        Event event = Main.getEvent();
        p.teleport(event.getSpawnLocation());
        event.eliminate(p);
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> p.setGameMode(GameMode.SPECTATOR), 1);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if (!EventManager.isEventRunning()) return;
        Event event = Main.getEvent();
        e.setDropItems(false);
        e.setCancelled(event.onBreakBlock(e, p, e.getBlock()));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        if (!EventManager.isEventRunning()) return;
        Event event = Main.getEvent();
        e.setCancelled(event.onPlaceBlock(e, p, e.getBlock(), e.getBlockReplacedState().getBlock()));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onAttack(EntityDamageByEntityEvent e) {
        Event event = Main.getEvent();
        if (!(e.getEntity() instanceof Player p)) return;
        if (!(e.getDamager() instanceof Player damager)) return;
        if (!EventManager.isEventRunning()) return;
        if (event.getEventState().equals(EventState.PLAYING)) {
            if (event.isDead(p)) {
                e.setCancelled(true);
                return;
            }
            e.setCancelled(event.onDamageByPlayer(damager, p));
            return;
        }
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageEvent e) {
        if (!EventManager.isEventRunning()) return;
        if (!e.getEntityType().equals(EntityType.PLAYER)) return;
        if (e.getCause().name().contains("ENTITY")) return;
        if (e.getCause().equals(EntityDamageEvent.DamageCause.CUSTOM) || e.getCause().equals((EntityDamageEvent.DamageCause.VOID))) {
            e.setCancelled(false);
            return;
        }
        Player p = (Player) e.getEntity();
        Event event = Main.getEvent();
        if (event.isDead(p)) {
            e.setCancelled(true);
            return;
        }
        e.setCancelled(event.onDamage(p, e));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDrop(PlayerDropItemEvent e) {
        if (!EventManager.isEventRunning()) return;
        Event event = Main.getEvent();
        if (event.isDead(e.getPlayer())) return;
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent e) {
        if (!EventManager.isEventRunning()) return;
        Player p = e.getEntity();
        Event event = Main.getEvent();
        e.deathMessage(Component.empty());
        e.getDrops().clear();
        event.eliminate(p);

        // Winner check
        event.end();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRespawn(PlayerRespawnEvent e) {
        if (!EventManager.isEventRunning()) return;
        Event event = Main.getEvent();
        Player p = e.getPlayer();
        p.getInventory().clear();
        p.teleport(event.getSpawnLocation());
        p.setGameMode(GameMode.SPECTATOR);
    }
}