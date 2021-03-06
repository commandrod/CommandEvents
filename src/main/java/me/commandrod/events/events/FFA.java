package me.commandrod.events.events;

import lombok.Getter;
import me.commandrod.commandapi.items.CommandItem;
import me.commandrod.commandapi.items.ItemUtils;
import me.commandrod.events.api.Counter;
import me.commandrod.events.api.event.Event;
import me.commandrod.events.api.event.EventState;
import me.commandrod.events.api.event.EventType;
import me.commandrod.events.utils.EventUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class FFA extends Event {

    @Getter
    private final Counter killsCounter;

    public FFA() {
        super(EventType.FFA, "השורד האחרון", -1);
        this.killsCounter = new Counter("Kills");
    }

    public List<String> getLines(Player player) {
        return Arrays.asList(
                "&7הריגות: &b" + this.getKillsCounter().getValue(player),
                EventUtils.border(this)
        );
    }

    public void setup(Player player) {
        player.getInventory().addItem(new ItemStack(Material.DIAMOND_AXE));
        player.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 4));
        player.getInventory().setItemInOffHand(new ItemStack(Material.SHIELD));
        player.getInventory().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
        player.getInventory().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
        player.getInventory().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
        player.getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
    }

    public void onEventStart() { EventUtils.border(this, 2, 2); }

    public void onEventEnd(Player winner) {
        this.getSpawnLocation().getWorld().getWorldBorder().reset();
        this.getKillsCounter().printResults();
    }

    public void onDeath(Player player) {
        Player killer = player.getKiller();
        if (killer == null) return;
        this.getKillsCounter().add(killer);
        Optional<ItemStack> opHead = goldenHead(player);
        opHead.ifPresent(head -> killer.getInventory().addItem(head));
    }

    public boolean onDamageByPlayer(Player attacker, Player damaged) { return !this.getEventState().equals(EventState.PLAYING); }
    public boolean onDamage(Player player, EntityDamageEvent event) { return event.getCause().equals(EntityDamageEvent.DamageCause.FALL); }

    private Optional<ItemStack> goldenHead(Player owner) {
        Optional<CommandItem> opItem = CommandItem.from("GOLDEN_HEAD");
        if (opItem.isEmpty()) return Optional.empty();
        ItemStack itemStack = ItemUtils.giveItem(owner, opItem.get(), "KILL", 1, false);
        ItemUtils.storeStringInItem(itemStack, "player", owner.getUniqueId().toString());
        ItemUtils.updateLore(itemStack);
        return Optional.of(itemStack);
    }
}
