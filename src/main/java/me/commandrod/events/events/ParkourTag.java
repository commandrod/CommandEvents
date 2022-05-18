package me.commandrod.events.events;

import me.commandrod.events.api.event.Event;
import me.commandrod.events.api.event.EventType;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public class ParkourTag extends Event {

    public ParkourTag() {
        super(EventType.PARKOUR_TAG, "תופסת הפארקור");
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
    public boolean onDamage(Player player, EntityDamageEvent event) { return true; }
    public boolean onInventoryClick(Player clicker, InventoryClickEvent event) { return false; }
}
