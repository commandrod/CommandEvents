package me.commandrod.events.events;

import me.commandrod.events.api.event.Event;
import me.commandrod.events.api.event.EventType;
import me.commandrod.events.api.event.Handle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

public class FallGuys extends Event {

    public FallGuys() {
        super(EventType.FALLGUYS, "פול גאייז");
    }

    public List<String> getLines(Player player) {
        return null;
    }

    public void activeEffect() { }
    public void preEventStart() { }
    public void onEventStart() { }
    public void onEventEnd(Player winner) { }
    public void onDeath(Player player) { }
    public void onRespawn(Player player) { }
    public boolean onBreakBlock(BlockBreakEvent event, Player breaker, Block block) { return true; }
    public boolean onPlaceBlock(BlockPlaceEvent event, Player placer, Block block, Block replacedBlock) { return true; }
    public boolean onDamageByPlayer(Player attacker, Player damaged) { return true; }
    public boolean onDamage(Player player, EntityDamageEvent event) { return event.getCause().equals(EntityDamageEvent.DamageCause.FALL); }
    public Handle onInventoryClick(Player clicker, InventoryClickEvent event) { return Handle.NONE; }
    public boolean onInteract(Player player, PlayerInteractEvent event) { return false; }
}
