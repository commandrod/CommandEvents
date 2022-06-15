package me.commandrod.events.events;

import me.commandrod.commandapi.cooldown.AdvancedCooldown;
import me.commandrod.commandapi.items.ItemUtils;
import me.commandrod.commandapi.utils.SoundUtils;
import me.commandrod.commandapi.utils.Utils;
import me.commandrod.events.api.event.Event;
import me.commandrod.events.api.event.EventType;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SnowDodge extends Event {

    public static final Material BLOCKTYPE = Material.BARREL;

    private record SnowBarrel(List<Block> blocks) {

        private static final HashMap<Block, SnowBarrel> barrelBlockMap = new HashMap<>();

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
                if (!newBlock.getType().equals(BLOCKTYPE)) continue;
                newBarrel.blocks.add(newBlock);
                newBarrel.blocks.add(newBlock.getRelative(BlockFace.DOWN));
                newBarrel.blocks.add(newBlock.getRelative(BlockFace.UP));
                barrelBlockMap.put(newBlock, newBarrel);
            }
            return newBarrel;
        }
    }

    private final AdvancedCooldown<SnowBarrel> commandCooldown = new AdvancedCooldown<>(5, true);

    public SnowDodge() {
        super(EventType.SNOWDODGE, "מחניים");
    }

    public List<String> getLines(Player player) {
        return null;
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

    private boolean hasPowerup(Player player) {
        return false;
    }

    public void onClick(Player clicker, Block block) {
        ItemStack snowball = ItemUtils.quickItem(Material.SNOWBALL, "כדור שלג&d", null, false);
        if (hasPowerup(clicker)) {
            snowball.setAmount(3);
        }
        clicker.getInventory().addItem(snowball);
        SoundUtils.playSound(clicker, Sound.ENTITY_ITEM_PICKUP);
    }

    public boolean onDamage(Player attacker, EntityDamageEvent event) {
        return !event.getCause().equals(EntityDamageEvent.DamageCause.CUSTOM);
    }

    public boolean onInteract(Player player, PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return false;
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return false;
        if (!clickedBlock.getType().equals(BLOCKTYPE)) return false;
        Player p = event.getPlayer();
        boolean cooldown = this.cooldown(p, clickedBlock);
        if (cooldown) return true;
        this.onClick(p, clickedBlock);
        return true;
    }

    public void preEventStart() { }
    public void onEventStart() { }
    public void onEventEnd(Player winner) { }
    public void onDeath(Player player) { }
    public void onRespawn(Player player) { }
    public boolean onBreakBlock(BlockBreakEvent event, Player breaker, Block block) { return true; }
    public boolean onPlaceBlock(BlockPlaceEvent event, Player placer, Block block, Block replacedBlock) { return true; }
    public boolean onDamageByPlayer(Player attacker, Player damaged) { return true; }
    public boolean onInventoryClick(Player clicker, InventoryClickEvent event) { return true; }
}
