package me.commandrod.events.events;

import lombok.Getter;
import lombok.Setter;
import me.commandrod.events.api.event.Event;
import me.commandrod.events.api.event.EventType;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class Simon extends Event {

    public Simon() {
        // 12000 ticks = 10 minutes
        super(EventType.SIMON, ":blue_circle:", "המלך אמר", 12000);
    }

    private Player king;
    @Getter
    @Setter
    private boolean isPvP = false;

    private Player getKing() {
        List<Player> managers = Bukkit.getOnlinePlayers().stream()
                .filter(player -> !this.getPlayers().contains(player))
                .collect(Collectors.toList());
        return managers.size() == 0 ? this.getPlayers().get(ThreadLocalRandom.current().nextInt(this.getPlayers().size())) : managers.get(0);
    }

    private boolean canBuild(Player player) {
        return !(player.hasPermission("active.admin") || this.king.getUniqueId() == player.getUniqueId());
    }

    public List<String> getLines(Player player) {
        String kingName = this.king == null ? "&cאין" : this.king.getName();
        return Collections.singletonList("&7המלך: &b" + kingName);
    }

    public boolean onDamageByPlayer(Player attacker, Player damaged) {
        if (attacker.getUniqueId().equals(this.king.getUniqueId())){
            this.eliminate(damaged);
            return true;
        }
        return !this.isPvP;
    }

    public boolean onDamage(Player player, EntityDamageEvent event) {
        return !this.isPvP;
    }

    public void onDeath(Player player) { for (Player players : this.getPlayers()) this.sendScoreboard(players); }
    public boolean onBreakBlock(BlockBreakEvent event, Player breaker, Block block) { return canBuild(breaker); }
    public boolean onPlaceBlock(BlockPlaceEvent event, Player placer, Block block, Block replacedBlock) { return canBuild(placer); }
    public void activeEffect() { }
    public void preEventStart() {
        this.king = getKing();
        this.sendScoreboard(this.getKing());
    }
    public void onEventStart() { }
    public void onEventEnd(Player winner) { }
    public void onRespawn(Player player) { }
    public void onScoreboardUpdate(Scoreboard scoreboard, Player player) { }
    public boolean onInventoryClick(Player clicker, InventoryClickEvent event) { return false; }
}