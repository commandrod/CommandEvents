package me.commandrod.events.items;

import me.commandrod.commandapi.items.CommandItem;
import me.commandrod.commandapi.items.ItemUtils;
import me.commandrod.commandapi.items.ability.Ability;
import me.commandrod.commandapi.items.rarity.Rarity;
import me.commandrod.commandapi.utils.Utils;
import me.commandrod.events.utils.EventUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GoldenHead extends CommandItem {

    public GoldenHead() {
        super(Material.PLAYER_HEAD, "GOLDEN_HEAD", "הראש של " + EventUtils.NULL, new Ability[]{}, new Rarity(Rarity.RarityType.RARE), null, null, true, false, false);
        ItemUtils.storeStringInItem(this.getItemStack(), "player", "6a478e6e-7d41-426c-a051-8758fd4ceaba");
    }

    public boolean rightClickAirAction(Player player, ItemStack itemStack) {
        int seconds = 4;
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, seconds*20, 2, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, seconds*20, 2, true, true));
        int amount = 8;
        int newAmount = player.getFoodLevel() + amount;
        int maxAmount = 20;
        if (maxAmount < newAmount) {
            newAmount =- maxAmount;
            player.setFoodLevel(maxAmount);
            player.setSaturation(Math.min(newAmount + player.getSaturation(), 20));
            return true;
        }
        player.setFoodLevel(newAmount);
        return true;
    }

    public void itemUpdateAction(ItemStack itemStack) {
        SkullMeta im = (SkullMeta) itemStack.getItemMeta();
        String uuidStr = ItemUtils.getStringFromItem(itemStack, "player");
        if (uuidStr.equals("")) return;
        UUID uuid = UUID.fromString(uuidStr);
        if (uuid == null) return;
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;
        im.setOwningPlayer(player);
        final List<Component> lore = im.lore() == null ? new ArrayList<>() : im.lore();
        int lines = 3;
        lore.add(lore.size() - lines, Utils.color("&7הראש הזה שייך לשחקן &6" + player.getName()));
        lore.add(lore.size() - lines, Utils.color("&7בלחיצה ימנית אתם תאכלו את הראש, ותקבלו אפקטים מיוחדים!"));
        im.displayName(Utils.color(this.getRarity().getColor() + "הראש של " + player.getName()));
        im.lore(lore);
        itemStack.setItemMeta(im);
    }

    public boolean shiftRightClickPlayerAction(Player player, Player player1, ItemStack itemStack) { return this.rightClickAirAction(player, itemStack); }
    public boolean rightClickPlayerAction(Player player, Player player1, ItemStack itemStack) { return this.rightClickAirAction(player, itemStack); }
    public boolean shiftRightClickAirAction(Player player, ItemStack itemStack) { return this.rightClickAirAction(player, itemStack); }
    public boolean rightClickBlockAction(Player player, ItemStack itemStack) { return this.rightClickAirAction(player, itemStack); }
    public boolean shiftRightClickBlockAction(Player player, ItemStack itemStack) { return this.rightClickAirAction(player, itemStack); }
    public boolean itemConsumeAction(Player player, ItemStack itemStack) { return true; }
    public boolean fishAction(Player player, ItemStack itemStack, PlayerFishEvent playerFishEvent) { return true; }
    public boolean inventoryAction(Player player, ItemStack itemStack, ItemStack itemStack1, InventoryClickEvent inventoryClickEvent) { return false; }
    public void activeEffect(Player player, ItemStack itemStack) { }
}
