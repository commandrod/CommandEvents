package me.commandrod.events.api.event;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

public interface AbstractEvent {

    void spawn();
    void countdown(int time);
    void eliminate(Player player);
    void revive(Player player);
    void lobby();
    void stop();
    void end();
    void winner(Player winner);
    void sendScoreboard(Player player);
    boolean isDead(Player player);

    EventType getType();
    List<Player> getPlayers();
    String getSubtitle();
    String getFriendlyName();
    int getActiveEffectTime();

    Location getSpawnLocation();
    void setSpawnLocation(Location spawnLocation);
    EventState getEventState();
    void setEventState(EventState eventState);

    List<String> getLines(Player player);
    void activeEffect();
    void preEventStart();
    void onEventStart();
    void onEventEnd(Player winner);
    void onDeath(Player player);
    void onRespawn(Player player);
    boolean onBreakBlock(BlockBreakEvent event, Player breaker, Block block);
    boolean onPlaceBlock(BlockPlaceEvent event, Player placer, Block block, Block replacedBlock);
    boolean onDamageByPlayer(Player attacker, Player damaged);
    boolean onDamage(Player player, EntityDamageEvent event);
    Handle onInventoryClick(Player clicker, InventoryClickEvent event);
    Handle onInteract(Player player, PlayerInteractEvent event);
}
