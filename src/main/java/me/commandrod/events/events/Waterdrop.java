package me.commandrod.events.events;

import lombok.Getter;
import lombok.Setter;
import me.commandrod.events.api.Counter;
import me.commandrod.events.api.event.Event;
import me.commandrod.events.api.event.EventState;
import me.commandrod.events.api.event.EventType;
import me.commandrod.events.utils.ConfigUtils;
import me.commandrod.events.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Getter @Setter
public class Waterdrop extends Event {

    private final int defaultTime = 15;
    private int round;
    private int time;
    private Counter furthestRoundCounter;
    private List<Player> passedPlayers;
    private List<Block> blocks;
    private boolean roundRunning;

    public Waterdrop() {
        super(EventType.WATERDROP, "Built by: ronii, ItzDuck_", "קפיצה למים");
        this.round = 0;
        this.time = this.defaultTime;
        this.furthestRoundCounter = new Counter("Furthest Round Reached");
        this.passedPlayers = new ArrayList<>();
        this.blocks = ConfigUtils.getBlocksBetween(ConfigUtils.getLocation("waterdrop-blocks-1"), ConfigUtils.getLocation("waterdrop-blocks-2"));
        this.roundRunning = false;
    }

    public List<String> getLines(Player player) {
        return Arrays.asList(
                "&7סבב: &b" + this.getRound(),
                "&7זמן לקפוץ: &b" + this.getTime()
        );
    }

    public void activeEffect() {
        for (Player player : this.getPlayers()){
            if (this.getPassedPlayers().contains(player)) continue;
            if (player.getLocation().getY() > this.getBlocks().get(0).getY() - 8) continue;
            Bukkit.broadcastMessage(Utils.color("&7" + player.getName() + " passed!"));
            this.getPassedPlayers().add(player);
        }
        if (this.getTime() > 0) this.setTime(this.getTime() - 1);
        if (this.getTime() == 0 || (this.getPassedPlayers().size() == this.getPlayers().size())){
            this.getPlayers().stream().filter(player -> player != null && !this.getPassedPlayers().contains(player)).forEach(this::eliminate);
            newRound();
        }
    }

    public void onEventStart() {
        for (Player player : this.getPlayers()){
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20*10000, 0, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20*10000, 0, false, false));
        }
        newRound();
    }

    public void onEventEnd(Player winner) {
        for (Block block : this.getBlocks()) block.setType(Material.AIR);
        this.getFurthestRoundCounter().set(winner, this.getRound());
        this.getFurthestRoundCounter().printResults();
    }

    public boolean onDamage(Player player, EntityDamageEvent event) {
        if (!this.getEventState().equals(EventState.PLAYING)) return true;
        if (!event.getCause().equals(EntityDamageEvent.DamageCause.FALL)) return true;
        if (event.isCancelled()) return true;
        if (this.isRoundRunning()) return true;
        this.eliminate(player);
        return true;
    }

    private void newRound() {
        Random rand = new Random();
        List<Block> selectedBlocks = new ArrayList<>();
        int amount = Math.min(this.getRound() * 5 + 7, this.getBlocks().size());
        for (int i = 0; i < amount - 1; i++) {
            Block selectedBlock = this.getBlocks().get(rand.nextInt(this.getBlocks().size()));
            if (selectedBlocks.contains(selectedBlock)){
                i--;
                continue;
            }
            selectedBlocks.add(selectedBlock);
        }
        for (Block block : this.getBlocks()){
            block.setType(Material.AIR);
        }
        for (Block block : selectedBlocks){
            block.setType(Material.RED_CONCRETE);
        }
        this.getPassedPlayers().clear();
        this.setRound(this.getRound() + 1);
        this.setTime(this.getDefaultTime());
        this.setRoundRunning(true);
        this.spawn();
        this.setRoundRunning(false);
    }

    public void preEventStart() { }
    public void onDeath(Player player) { this.getFurthestRoundCounter().set(player, this.getRound()); }
    public void onRespawn(Player player) { }
    public void onScoreboardUpdate(Scoreboard scoreboard, Player player) { }
    public boolean onBreakBlock(BlockBreakEvent event, Player breaker, Block block) { return true; }
    public boolean onPlaceBlock(BlockPlaceEvent event, Player placer, Block block, Block replacedBlock) { return true; }
    public boolean onDamageByPlayer(Player attacker, Player damaged) { return true; }
    public boolean onInventoryClick(Player clicker, InventoryClickEvent event) { return false; }
}
