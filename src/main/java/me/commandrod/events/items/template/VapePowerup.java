package me.commandrod.events.items.template;

import me.commandrod.commandapi.cooldown.CommandCooldown;
import me.commandrod.commandapi.items.CommandItem;
import me.commandrod.commandapi.items.ability.Ability;
import me.commandrod.commandapi.items.rarity.Rarity;
import me.commandrod.commandapi.items.rarity.RarityType;
import me.commandrod.commandapi.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

public abstract class VapePowerup extends CommandItem {

    public VapePowerup(Material material, String id, String type, String description, int cooldown) {
        super(material, id + "_VAPE", "וייפ " + type, new Ability[]{}, new Rarity(RarityType.UNCOMMON),
                Utils.listFromString("&7" + description + "\n&8Cooldown: " + cooldown + "s"),
                null, false, false, false, new CommandCooldown(cooldown));
    }

    public abstract boolean rightClickPlayerAction(Player player, Player player1, ItemStack itemStack);
    public abstract boolean rightClickBlockAction(Player player, ItemStack itemStack);
    public abstract boolean rightClickAirAction(Player player, ItemStack itemStack);
    public boolean shiftRightClickPlayerAction(Player player, Player player1, ItemStack itemStack) { return false; }
    public boolean shiftRightClickAirAction(Player player, ItemStack itemStack) { return false; }
    public boolean shiftRightClickBlockAction(Player player, ItemStack itemStack) { return false; }
    public boolean itemConsumeAction(Player player, ItemStack itemStack) { return false; }
    public boolean fishAction(Player player, ItemStack itemStack, PlayerFishEvent playerFishEvent) { return false; }
    public boolean inventoryAction(Player player, ItemStack itemStack, ItemStack itemStack1, InventoryClickEvent inventoryClickEvent) { return false; }
    public boolean activeEffect(Player player, ItemStack itemStack) { return false; }
    public void itemUpdateAction(ItemStack itemStack) { }
}
