package me.commandrod.events.events;

import me.commandrod.commandapi.utils.ConfigUtils;
import me.commandrod.commandapi.utils.Utils;
import me.commandrod.events.Main;
import me.commandrod.events.api.Counter;
import me.commandrod.events.api.event.Event;
import me.commandrod.events.api.event.EventState;
import me.commandrod.events.api.event.EventType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class Waterdrop extends Event {

    private final int DEFAULT_TIME = 15;
    private final List<Player> passedPlayers;
    private final Counter furthestRoundCounter;

    private int round;
    private int time;
    private List<Block> blocks;
    private boolean roundRunning;

    public Waterdrop() {
        super(EventType.WATERDROP, "קפיצה למים");
        this.round = 0;
        this.time = this.DEFAULT_TIME;
        this.furthestRoundCounter = new Counter("Furthest Round Reached");
        this.passedPlayers = new ArrayList<>();
        this.roundRunning = false;
        Optional<Location> opBlocks1 = ConfigUtils.getInstance().getLocation("waterdrop-blocks-1");
        Optional<Location> opBlocks2 = ConfigUtils.getInstance().getLocation("waterdrop-blocks-2");
        if (opBlocks1.isEmpty() || opBlocks2.isEmpty()) {
            Main.getPlugin().getLogger().severe("Error getting blocks for waterdrop!");
            return;
        }
        this.blocks = ConfigUtils.getInstance().getBlocksBetween(opBlocks1.get(), opBlocks2.get());
    }

    public List<String> getLines(Player player) {
        return Arrays.asList(
                "&7סבב: &b" + this.round,
                "&7זמן לקפוץ: &b" + this.time
        );
    }

    public void activeEffect() {
        for (Player player : this.getPlayers()){
            if (this.passedPlayers.contains(player)) continue;
            if (player.getLocation().getY() > this.blocks.get(0).getY() - 8) continue;
            Bukkit.broadcast(Utils.color("&7" + player.getName() + " passed!"));
            this.passedPlayers.add(player);
        }
        if (this.time > 0) this.time--;
        if (this.time == 0 || (this.passedPlayers.size() == this.getPlayers().size())){
            this.getPlayers().stream().filter(player -> player != null && !this.passedPlayers.contains(player)).forEach(this::eliminate);
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
        for (Block block : this.blocks) block.setType(Material.AIR);
        this.furthestRoundCounter.set(winner, this.round);
        this.furthestRoundCounter.printResults();
    }

    public boolean onDamage(Player player, EntityDamageEvent event) {
        if (!this.getEventState().equals(EventState.PLAYING)) return true;
        if (!event.getCause().equals(EntityDamageEvent.DamageCause.FALL)) return true;
        if (event.isCancelled()) return true;
        if (this.roundRunning) return true;
        this.eliminate(player);
        return true;
    }

    private void newRound() {
        Random rand = new Random();
        List<Block> selectedBlocks = new ArrayList<>();
        int amount = Math.min(this.round * 5 + 7, this.blocks.size());
        for (int i = 0; i < amount - 1; i++) {
            Block selectedBlock = this.blocks.get(rand.nextInt(this.blocks.size()));
            if (selectedBlocks.contains(selectedBlock)){
                i--;
                continue;
            }
            selectedBlocks.add(selectedBlock);
        }
        for (Block block : this.blocks){
            block.setType(Material.AIR);
        }
        for (Block block : selectedBlocks){
            block.setType(Material.RED_CONCRETE);
        }
        this.passedPlayers.clear();
        this.round++;
        this.time = DEFAULT_TIME;
        this.roundRunning = true;
        this.spawn();
        this.roundRunning = false;
    }

    public void preEventStart() { }
    public void onDeath(Player player) { this.furthestRoundCounter.set(player, this.round); }
    public void onRespawn(Player player) { }
    public boolean onBreakBlock(BlockBreakEvent event, Player breaker, Block block) { return true; }
    public boolean onPlaceBlock(BlockPlaceEvent event, Player placer, Block block, Block replacedBlock) { return true; }
    public boolean onDamageByPlayer(Player attacker, Player damaged) { return true; }
    public boolean onInventoryClick(Player clicker, InventoryClickEvent event) { return false; }
    public boolean onInteract(Player player, PlayerInteractEvent event) { return false; }
}
