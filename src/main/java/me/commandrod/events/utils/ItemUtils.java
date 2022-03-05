package me.commandrod.events.utils;

import me.commandrod.events.Main;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class ItemUtils {

    private static final JavaPlugin plugin = Main.getPlugin();

    public static ItemStack newItem(Material material, String displayName, List<String> lore, boolean enchanted) {
        ItemStack i = new ItemStack(material);
        ItemMeta im = i.getItemMeta();
        im.setDisplayName(Utils.color(displayName));
        im.setUnbreakable(true);
        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
        if (enchanted) im.addEnchant(Enchantment.ARROW_KNOCKBACK, 1, false);
        if (lore != null){
            Utils.colorList(lore);
            im.setLore(lore);
        }
        i.setItemMeta(im);
        return i;
    }

    public static ItemStack newItem(Material material, String displayName, String id, List<String> lore, boolean enchanted){
        ItemStack i = newItem(material, displayName, lore, enchanted);
        storeStringInItem(i, "id", id);
        return i;
    }

    public static void storeStringInItem(ItemStack itemStack, String key, String value) {
        ItemMeta meta = itemStack.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(new NamespacedKey(plugin, key), PersistentDataType.STRING, value);
        itemStack.setItemMeta(meta);
    }

    public static String getStringFromItem(ItemStack itemStack, String key) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return "";
        PersistentDataContainer data = meta.getPersistentDataContainer();
        return !data.has(new NamespacedKey(plugin, key), PersistentDataType.STRING) ? "" : data.get(new NamespacedKey(plugin, key), PersistentDataType.STRING);
    }
}