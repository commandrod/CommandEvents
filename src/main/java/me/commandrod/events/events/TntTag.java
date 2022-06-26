package me.commandrod.events.events;

import me.commandrod.commandapi.cooldown.CommandCooldown;
import me.commandrod.commandapi.items.ItemUtils;
import me.commandrod.commandapi.utils.Utils;
import me.commandrod.events.Main;
import me.commandrod.events.api.event.EventState;
import me.commandrod.events.api.event.EventType;
import me.commandrod.events.api.event.template.RoundEvent;
import me.commandrod.events.utils.EventUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TntTag extends RoundEvent {

    private final List<Player> taggers;
    private final CommandCooldown cooldownManager;

    private ItemStack tnt() { return ItemUtils.quickItem(Material.TNT, "&cאתה התופס!", null, false); }

    public TntTag() {
        super(EventType.TNTTAG, "התופס המתפוצץ", 45);
        this.taggers = new ArrayList<>();
        this.cooldownManager = new CommandCooldown(.3f, false);
    }

    public List<String> getExtraLines(Player player) {
        return Collections.singletonList(
                "&7מספר התופסים: &b" + this.taggers.size()
        );
    }

    public void timeOver() {
        for (Player player : this.getPlayers()
                .stream()
                .filter(this.taggers::contains)
                .collect(Collectors.toList()))
            this.eliminate(player);
        this.randomTag();
    }

    public void timeRunning() { }

    public void onEventStart() { this.randomTag(); }

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
        this.resetTime();
        int amount = Math.floorDiv(this.getPlayers().size(), 3);
        int finalAmount = Math.max(amount, 1);
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            for (int i = 0; i < finalAmount; i++){
            List<Player> filtered = this.getPlayers()
                    .stream()
                    .filter(Predicate.not(this.taggers::contains))
                    .collect(Collectors.toList());
            Player player = filtered.get(EventUtils.random(this.getPlayers().size()));
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
}
