package me.commandrod.events.events;

import lombok.Getter;
import me.commandrod.commandapi.items.CommandItem;
import me.commandrod.events.api.CooldownManager;
import me.commandrod.events.api.Counter;
import me.commandrod.events.api.event.Event;
import me.commandrod.events.api.event.EventState;
import me.commandrod.events.api.event.EventType;
import me.commandrod.events.utils.ItemUtils;
import me.commandrod.events.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.Color;

import java.util.*;

@Getter
public class Netanya extends Event {

    private final Counter stabCounter;
    private final Counter killCounter;
    private final List<UUID> red;
    private final List<UUID> blue;
    private final HashMap<Player, List<UUID>> playerTeam;
    private final CooldownManager cooldownManager;

    // Hey github peeps, this mode is not meant to be offensive lol
    // Also this isn't a serious event we only did it once and I'm keeping the code in case I wanna do it again

    public Netanya() {
        super(EventType.NETANYA, "תהיו מוכנים, הצרפתים מתקרבים!", "סימולטור נתניה - הדוקר האחרון");
        this.stabCounter = new Counter("Stabs");
        this.killCounter = new Counter("Kills");
        this.playerTeam = new HashMap<>();
        this.red = new ArrayList<>();
        this.blue = new ArrayList<>();
        this.cooldownManager = new CooldownManager(1);
    }

    public List<String> getLines(Player player) {
        return Arrays.asList(
                "&7הריגות: &b" + this.getKillCounter().getValue(player),
                "&7דקירות: &b" + this.getStabCounter().getValue(player)
        );
    }

    public void preEventStart() {
        this.getPlayers().forEach(player -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "team leave " + player.getName());
            randomTeam(player);
            giveItems(player);
        });
    }

    public void onEventStart() {
        this.getPlayers().forEach(player -> player.setGlowing(true));
    }

    public boolean onDamageByPlayer(Player attacker, Player damaged) {
        if (!this.getEventState().equals(EventState.PLAYING)) return true;
        if (this.getPlayerTeam().get(attacker).equals(this.getPlayerTeam().get(damaged))){
            if (this.getCooldownManager().call(attacker)) attacker.sendMessage(Utils.color("&cאי אפשר להרביץ לערס מאותו גאנג!"));
            return true;
        }
        this.getStabCounter().add(attacker);
        return false;
    }

    public void onEventEnd(Player winner) {
        this.getKillCounter().printResults();
        this.getStabCounter().printResults();
        this.getPlayers().forEach(player -> player.setPlayerListName(player.getName()));
    }

    public void onDeath(Player player) {
        Random r = new Random();
        if (this.getBlue().size() == 0){
            this.winner(Bukkit.getPlayer(this.getRed().get(r.nextInt(this.getRed().size()))));
        } else if (this.getRed().size() == 0){
            this.winner(Bukkit.getPlayer(this.getBlue().get(r.nextInt(this.getBlue().size()))));
        }
    }

    public void activeEffect() { }
    public void onScoreboardUpdate(Scoreboard scoreboard, Player player) { }
    public void onRespawn(Player player) { }
    public boolean onBreakBlock(BlockBreakEvent event, Player breaker, Block block) { return false; }
    public boolean onPlaceBlock(BlockPlaceEvent event, Player placer, Block block, Block replacedBlock) { return false; }
    public boolean onDamage(Player player, EntityDamageEvent event) { return this.getEventState().equals(EventState.PLAYING) || event.getCause().equals(EntityDamageEvent.DamageCause.FALL); }
    public boolean onInventoryClick(Player clicker, InventoryClickEvent event) { return true; }

    private ItemStack leather(Material material, Color color){
        ItemStack i = new ItemStack(material);
        LeatherArmorMeta im = (LeatherArmorMeta) i.getItemMeta();
        im.setColor(color);
        i.setItemMeta(im);
        return i;
    }

    private ItemStack[] getArmor(Player player){
        Color color = this.getPlayerTeam().get(player).equals(this.getBlue()) ? Color.BLUE : Color.RED;
        return new ItemStack[]{
                leather(Material.LEATHER_BOOTS, color),
                leather(Material.LEATHER_LEGGINGS, color),
                leather(Material.LEATHER_CHESTPLATE, color),
                leather(Material.LEATHER_HELMET, color)
        };
    }

    private void giveItems(Player player){
        Random r = new Random();
        player.getInventory().setArmorContents(getArmor(player));
        switch (r.nextInt(3)){
            case 0:
                player.getInventory().addItem(ItemUtils.newItem(Material.STONE_SWORD, "&fסכין מאבן", getKnifeLore(), false));
                player.getInventory().setHelmet(getKipa());
                break;
            case 1:
                player.getInventory().addItem(ItemUtils.newItem(Material.GOLDEN_SWORD, "&6סכין זהב פרו מקס", getKnifeLore(), false));
                player.getInventory().setHelmet(getKipa());
                break;
            case 2:
                player.getInventory().addItem(ItemUtils.newItem(Material.WOODEN_SWORD, "&fסכין מעץ", getKnifeLore(), false));
                player.getInventory().setHelmet(ItemUtils.newItem(Material.IRON_HELMET, "&fכיפה", getKipaLore(), false));
                break;
        }
        player.getInventory().addItem(randomVape());
    }

    private ItemStack randomVape(){
        List<CommandItem> vapes = Arrays.asList(CommandItem.getCommandItem("STRAWBERRY_VAPE"), CommandItem.getCommandItem("APPLE_VAPE"), CommandItem.getCommandItem("WEED_VAPE"));
        Random r = new Random();
        return vapes.get(r.nextInt(vapes.size())).getItemStack();
    }

    private ItemStack getKipa(){
        ItemStack is = ItemUtils.newItem(Material.LEATHER_HELMET, "&fכיפה", getKipaLore(), false);
        LeatherArmorMeta im = (LeatherArmorMeta) is.getItemMeta();
        Random r = new Random();
        Color randomColor = Color.fromBGR(r.nextInt(255), r.nextInt(255), r.nextInt(255));
        im.setColor(randomColor);
        is.setItemMeta(im);
        return is;
    }

    private List<String> getKipaLore(){ return Arrays.asList("&7", "&bדניאל יונה היה כאן - 16:58 25/02/2022"); }
    private List<String> getKnifeLore(){ return Arrays.asList("&7נוצר בנתניה העתיקה", "&7", "&cנחתם על ידי שלמה ארצי - 16:37 25/02/2022"); }

    private void addToBlue(Player player){
        this.getBlue().add(player.getUniqueId());
        this.getPlayerTeam().put(player, this.getBlue());
        player.setPlayerListName(Utils.color("&9" + player.getName()));
        player.setDisplayName(Utils.color("&9" + player.getName()));

    }

    private void addToRed(Player player){
        this.getRed().add(player.getUniqueId());
        this.getPlayerTeam().put(player, this.getRed());
        player.setPlayerListName(Utils.color("&c" + player.getName()));
    }

    private void randomTeam(Player player){
        if (this.getRed().size() > this.getBlue().size()){
            this.addToBlue(player);
        } else if (this.getRed().size() < this.getBlue().size()){
            this.addToRed(player);
        } else if (new Random().nextBoolean()) {
            this.addToRed(player);
        } else {
            this.addToBlue(player);
        }
    }
}
