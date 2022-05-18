package me.commandrod.events.events;

import me.commandrod.commandapi.cooldown.CommandCooldown;
import me.commandrod.commandapi.items.ItemUtils;
import me.commandrod.commandapi.utils.Utils;
import me.commandrod.events.Main;
import me.commandrod.events.api.event.Event;
import me.commandrod.events.api.event.EventState;
import me.commandrod.events.api.event.EventType;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class TntTag extends Event {

    private final List<Player> taggers;
    private final CommandCooldown cooldownManager;
    private int time;

    private final int defaultTime = 45;

    private ItemStack tnt(){ return ItemUtils.quickItem(Material.TNT, "&cאתה התופס!", null, false); }

    public TntTag() {
        super(EventType.TNTTAG, "התופס המתפוצץ");
        this.taggers = new ArrayList<>();
        this.cooldownManager = new CommandCooldown(.3f, false);
        this.time = defaultTime;
    }

    public List<String> getLines(Player player) {
        return Arrays.asList(
                "&7זמן לפיצוץ: &b" + this.time,
                "&7מספר התופסים: &b" + this.taggers.size()
        );
    }

    // Time Manager
    public void activeEffect() {
        if (this.time > 0) this.time--;
        if (this.time != 0) return;
        for (Player player : this.getPlayers().stream().filter(this.taggers::contains).collect(Collectors.toList()))
            this.eliminate(player);
        this.randomTag();
    }

    public void preEventStart() { for (Player player : this.getPlayers()) player.setGameMode(GameMode.ADVENTURE); }

    public void onEventStart() { this.randomTag(); }

    public void onEventEnd(Player winner) { this.taggers.clear(); }

    public boolean onDamageByPlayer(Player attacker, Player damaged) {
        if (!this.taggers.contains(attacker)) return true;
        if (this.taggers.contains(damaged)) return true;
        if (!this.cooldownManager.call(attacker) || !this.cooldownManager.call(damaged)) return true;
        this.tag(damaged, attacker);
        this.untag(attacker);
        return true;
    }

    public void onDeath(Player player) {
        if (!this.taggers.contains(player)) return;
        this.untag(player);
        player.getLocation().createExplosion(4f, false, false);
    }

    private void randomTag(){
        if (!this.getEventState().equals(EventState.PLAYING)) return;
        this.time = this.defaultTime;
        int amount = Math.floorDiv(this.getPlayers().size(), 3);
        int finalAmount = Math.max(amount, 1);
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
        for (int i = 0; i < finalAmount; i++){
            List<Player> filtered = this.getPlayers().stream()
                    .filter(p -> !this.taggers.contains(p))
                    .collect(Collectors.toList());
            Player player = filtered.get(ThreadLocalRandom.current().nextInt(this.getPlayers().size() - 1));
            this.tag(player, null);
        }}, 50);
    }

    private void untag(Player player){
        this.taggers.remove(player);
        player.getInventory().clear();
    }

    private void tag(Player player, Player attacker){
        if (this.taggers.contains(player)) return;
        this.taggers.add(player);
        for (int i = 0; i < 9; i++){
            player.getInventory().setItem(i, tnt());
        }
        player.getInventory().setHelmet(tnt());
        if (attacker == null){
            Bukkit.broadcast(Utils.color("&7" + player.getName() + " has been tagged!"));
            return;
        }
        Bukkit.broadcast(Utils.color("&7" + player.getName() + " has been tagged by " + attacker.getName() + "!"));
    }

    public void onRespawn(Player player) { }
    public boolean onBreakBlock(BlockBreakEvent event, Player breaker, Block block) { return true; }
    public boolean onPlaceBlock(BlockPlaceEvent event, Player placer, Block block, Block replacedBlock) { return true; }
    public boolean onDamage(Player attacker, EntityDamageEvent event) { return true; }
    public boolean onInventoryClick(Player clicker, InventoryClickEvent event) { return false; }
}
