package me.commandrod.events.events;

import lombok.Getter;
import me.commandrod.commandapi.items.CommandItem;
import me.commandrod.commandapi.items.ItemUtils;
import me.commandrod.commandapi.utils.MessageUtils;
import me.commandrod.events.api.Counter;
import me.commandrod.events.api.event.Event;
import me.commandrod.events.api.event.EventState;
import me.commandrod.events.api.event.EventType;
import me.commandrod.events.utils.EventUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Arrays;
import java.util.List;

public class FFA extends Event {

    @Getter
    private final Counter killsCounter;

    public FFA() {
        super(EventType.FFA, "Built by: ronii", "השורד האחרון");
        this.killsCounter = new Counter("Kills");
    }

    private ItemStack goldenHead(Player owner){
        ItemStack itemStack = ItemUtils.giveItem(owner, CommandItem.getCommandItem("GOLDEN_HEAD"), "KILL", 1, false);
        ItemUtils.storeStringInItem(itemStack, "player", owner.getUniqueId().toString());
        ItemUtils.updateLore(itemStack);
        return itemStack;
    }

    public void preEventStart() {
        this.getPlayers().forEach(player -> {
            player.getInventory().setItem(0, new ItemStack(Material.DIAMOND_SWORD));
            player.getInventory().setItem(1, new ItemStack(Material.COOKED_BEEF, 4));
            player.getInventory().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
            player.getInventory().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
            player.getInventory().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
            player.getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
        });
    }

    public void onEventStart() { EventUtils.border(this, 2, 2); }

    public void onEventEnd(Player winner) {
        this.getSpawnLocation().getWorld().getWorldBorder().reset();
        this.getKillsCounter().printResults();
    }

    public void onDeath(Player player) {
        Player killer = player.getKiller();
        if (killer == null) return;
        killer.getInventory().addItem(goldenHead(player));
        this.getKillsCounter().add(killer);
    }

    public List<String> getLines(Player player) {
        return Arrays.asList(
                "&7הריגות: &b" + this.getKillsCounter().getValue(player),
                EventUtils.border(this)
        );
    }

    public void activeEffect() {}
    public void onScoreboardUpdate(Scoreboard scoreboard, Player player) { }
    public void onRespawn(Player player) { }
    public boolean onBreakBlock(BlockBreakEvent event, Player breaker, Block block) { return true; }
    public boolean onPlaceBlock(BlockPlaceEvent event, Player placer, Block block, Block replacedBlock) { return true; }
    public boolean onDamageByPlayer(Player attacker, Player damaged) { return !this.getEventState().equals(EventState.PLAYING); }
    public boolean onDamage(Player player, EntityDamageEvent event) { return event.getCause().equals(EntityDamageEvent.DamageCause.FALL); }
    public boolean onInventoryClick(Player clicker, InventoryClickEvent event) { return false; }
}
