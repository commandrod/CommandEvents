package me.commandrod.events.events;

import lombok.Getter;
import me.commandrod.commandapi.items.ItemUtils;
import me.commandrod.events.api.Counter;
import me.commandrod.events.api.event.Event;
import me.commandrod.events.api.event.EventManager;
import me.commandrod.events.api.event.EventState;
import me.commandrod.events.api.event.EventType;
import me.commandrod.events.utils.EventUtils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class Spleef extends Event {

    private final ItemStack spleefShovel;
    private final List<Block> destroyedBlocks;
    private final Counter blocksCounter;
    private final Counter snowballCounter;

    public Spleef() {
        super(EventType.SPLEEF, "ספליף");
        this.destroyedBlocks = new ArrayList<>();
        this.blocksCounter = new Counter("Blocks");
        this.snowballCounter = new Counter("Snowballs");
        ItemStack spleefer = ItemUtils.quickItem(Material.DIAMOND_SHOVEL, "&cספליפר", Arrays.asList("", "&cמה אתה עושה? תתרכז באיוונט!"), true);
        ItemMeta im = spleefer.getItemMeta();
        im.addEnchant(Enchantment.DIG_SPEED, 5, true);
        spleefer.setItemMeta(im);
        this.spleefShovel = spleefer;
    }

    public List<String> getLines(Player player){
        return Arrays.asList(
                "&7בלוקים שהרסת: &b" + this.getBlocksCounter().getValue(player),
                "&7כדורי שלג שזרקת: &b" + this.getSnowballCounter().getValue(player),
                EventUtils.border(this)
        );
    }

    public void preEventStart() {
        this.getPlayers().forEach(player -> player.getInventory().addItem(getSpleefShovel()));
        this.getSpawnLocation().getWorld().getWorldBorder().reset();
    }

    public void onEventStart() {
        this.getPlayers().forEach(player -> player.setGameMode(GameMode.SURVIVAL));
        EventUtils.border(this, 3, 4);
    }

    public boolean onBreakBlock(BlockBreakEvent event, Player breaker, Block block) {
        if (!this.getEventState().equals(EventState.PLAYING)) return true;
        if (!block.getType().equals(Material.SNOW_BLOCK)) return true;
        event.setDropItems(false);
        breaker.getInventory().addItem(new ItemStack(Material.SNOWBALL));
        this.getBlocksCounter().add(breaker);
        this.getDestroyedBlocks().add(block);
        return false;
    }

    public void activeEffect(){
        if (!EventManager.isEventRunning()) return;
        if (this.getPlayers().size() == 0) return;
        for (Player player : this.getPlayers()){
            if (player.getLocation().getBlock().getType().equals(Material.WATER)) this.eliminate(player);
        }
    }

    public void onEventEnd(Player winner) {
        this.getSpawnLocation().getWorld().getWorldBorder().reset();
        this.getSnowballCounter().printResults();
        this.getBlocksCounter().printResults();
        this.getDestroyedBlocks().forEach(block -> block.setType(Material.SNOW_BLOCK));
    }

    public void onDeath(Player player) { }
    public void onRespawn(Player player) { }
    public boolean onPlaceBlock(BlockPlaceEvent event, Player placer, Block block, Block replacedBlock) { return true; }
    public boolean onDamageByPlayer(Player attacker, Player damaged) { return true; }
    public boolean onDamage(Player player, EntityDamageEvent event) { return event.getCause().equals(EntityDamageEvent.DamageCause.FALL); }
    public boolean onInventoryClick(Player clicker, InventoryClickEvent event) { return false; }
    public boolean onInteract(Player player, PlayerInteractEvent event) { return false; }
}
