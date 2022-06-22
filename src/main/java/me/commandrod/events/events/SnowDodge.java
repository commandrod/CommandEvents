package me.commandrod.events.events;

import me.commandrod.commandapi.cooldown.AdvancedCooldown;
import me.commandrod.commandapi.items.ItemUtils;
import me.commandrod.commandapi.other.RepeatingTask;
import me.commandrod.commandapi.utils.ConfigUtils;
import me.commandrod.commandapi.utils.SoundUtils;
import me.commandrod.commandapi.utils.Utils;
import me.commandrod.events.api.Counter;
import me.commandrod.events.api.event.Event;
import me.commandrod.events.api.event.EventType;
import me.commandrod.events.api.event.Handle;
import me.commandrod.events.utils.EventUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class SnowDodge extends Event {

    private final Counter snowballCounter, killCounter;
    private final AdvancedCooldown<SnowBarrel> commandCooldown = new AdvancedCooldown<>(5, true);

    public static final Material BLOCKTYPE = Material.BARREL;

    public SnowDodge() {
        super(EventType.SNOWDODGE, "מלחמת השלג");
        this.killCounter = new Counter("Kills");
        this.snowballCounter = new Counter("Snowballs Picked Up");
    }

    public List<String> getLines(Player player) {
        return Arrays.asList(
                "&7כדורי שלג שהרמת: &b" + this.snowballCounter.getValue(player),
                "&7הריגות: &b" + this.killCounter.getValue(player),
                EventUtils.border(this)
        );
    }

    public void activeEffect() {
        for (Player player : this.getPlayers()) {
            Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
            if (!block.getType().equals(Material.LIME_WOOL)) continue;
            if (player.getHealth() >= 20) continue;
            SoundUtils.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 21, 2, false, false));
        }
    }

    public void preEventStart() {
        for (Player player : this.getPlayers())
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 2000000, 2, false, false));
    }

    public void onEventStart() {
        EventUtils.border(this, 2.5F);
        final ItemStack[] powerups = new ItemStack[]{
                powerup(Material.POTION, "EXTRA_DAMAGE"),
                powerup(Material.SNOWBALL, "EXTRA_BALLS"),
                powerup(Material.FEATHER, "INVIS")
        };
        int cooldown = 600; // 600 ticks = 30 seconds
        new RepeatingTask(cooldown, cooldown) {
            public void run() {
                Optional<Location> opLoc = ConfigUtils.getInstance().getLocation("snowdodge-powerup");
                if (opLoc.isEmpty()) {
                    Bukkit.broadcast(Utils.color("&cקרתה בעיה עם השדרוגים! אנא פנו לאדמין."));
                    this.cancel();
                    return;
                }
                ItemStack randomItem = powerups[ThreadLocalRandom.current().nextInt(powerups.length)];
                Location loc = opLoc.get();
                Item item = (Item) loc.getWorld().spawnEntity(loc, EntityType.DROPPED_ITEM);
                item.setItemStack(randomItem);
                item.setGlowing(true);
                item.setCustomNameVisible(true);
                item.customName(Utils.color("&cשדרוג"));
            }
        };
    }

    public void onEventEnd(Player winner) {
        this.killCounter.printResults();
        this.snowballCounter.printResults();
    }

    public void onDeath(Player player) {
        Player killer = player.getKiller();
        if (killer == null) return;
        this.killCounter.add(killer);
    }

    public boolean onDamageByPlayer(Player player, Player damager) {
        this.killCounter.add(damager);
        return false;
    }

    public Handle onInteract(Player player, PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return Handle.NONE;
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return Handle.TRUE;
        if (!clickedBlock.getType().equals(BLOCKTYPE)) return Handle.TRUE;
        Player p = event.getPlayer();
        boolean cooldown = this.cooldown(p, clickedBlock);
        if (cooldown) return Handle.TRUE;
        this.onClick(p);
        return Handle.TRUE;
    }

    public boolean cooldown(Player clicker, Block block) {
        final SnowBarrel barrel = SnowBarrel.from(block);
        boolean cooldownOver = this.commandCooldown.call(barrel);
        if (!cooldownOver) {
            String msg = "&c" + "התיבה מייצרת כדורים! חזור בעוד " + this.commandCooldown.getSecondsLeft(barrel) + " שניות";
            clicker.sendActionBar(Utils.color(msg));
            SoundUtils.playSound(clicker, Sound.BLOCK_BARREL_CLOSE);
            return true;
        }
        return false;
    }

    public void damagePlayer(Player player, Player damager) {
        PowerUpManager manager = PowerUpManager.from(damager);
        double damage = manager.activePowerUps.contains(PowerUp.EXTRA_DAMAGE) ? 3 : 2;
        player.damage(damage, damager);
    }


    public void onClick(Player clicker) {
        this.snowballCounter.add(clicker);
        ItemStack snowball = ItemUtils.quickItem(Material.SNOWBALL, "כדור שלג&d", null, false);
        PowerUpManager powerups = PowerUpManager.from(clicker);
        float pitch = 1F;
        if (powerups.activePowerUps.contains(PowerUp.EXTRA_BALLS)) {
            pitch = 1.5F;
            snowball.setAmount(2);
        }
        clicker.getInventory().addItem(snowball);
        SoundUtils.playSound(clicker, Sound.ENTITY_ITEM_PICKUP, pitch, 1F);
    }

    private ItemStack powerup(Material material, String id) {
        return ItemUtils.quickItem(material, "&cשדרוג", "SNOWDODGE_" + id, null, true);
    }

    private record SnowBarrel(List<Block> blocks) {

        private static final HashMap<Block, SnowBarrel> barrelBlockMap = new HashMap<>();

        private void addAllBlocks(Block block) {
            this.blocks.add(block);
            barrelBlockMap.put(block, this);
        }

        public void addBlock(Block block) {
            this.addAllBlocks(block);
            this.addAllBlocks(block.getRelative(BlockFace.DOWN));
            this.addAllBlocks(block.getRelative(BlockFace.UP));
        }

        public static SnowBarrel from(Block block) {
            final BlockFace[] directions = new BlockFace[] {
                    BlockFace.NORTH,
                    BlockFace.EAST,
                    BlockFace.SOUTH,
                    BlockFace.WEST,
                    BlockFace.NORTH_EAST,
                    BlockFace.NORTH_WEST,
                    BlockFace.SOUTH_EAST,
                    BlockFace.SOUTH_WEST,
                    BlockFace.SELF
            };

            if (barrelBlockMap.containsKey(block)) {
                return barrelBlockMap.get(block);
            }

            SnowBarrel newBarrel = new SnowBarrel(new ArrayList<>());
            for (BlockFace direction : directions) {
                Block newBlock = block.getRelative(direction);
                newBarrel.addBlock(newBlock);
            }
            return newBarrel;
        }
    }

    public static record PowerUpManager(Player player, List<PowerUp> activePowerUps) {

        private static final HashMap<UUID, PowerUpManager> powerUpMap = new HashMap<>();

        public static PowerUpManager from(Player player) {
            if (!powerUpMap.containsKey(player.getUniqueId())) {
                PowerUpManager powerUpManager = new PowerUpManager(player, new ArrayList<>());
                powerUpMap.put(player.getUniqueId(), powerUpManager);
            }
            return powerUpMap.get(player.getUniqueId());
        }
    }

    public enum PowerUp {
        EXTRA_BALLS,
        EXTRA_DAMAGE
    }
}
