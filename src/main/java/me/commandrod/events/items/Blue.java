package me.commandrod.events.items;

import me.commandrod.commandapi.utils.SoundUtils;
import me.commandrod.events.items.template.VapePowerup;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Blue extends VapePowerup {

    public Blue(){
        super(Material.APPLE, "BLUE", "בטעם כחול", "נותן למשתמש אפקט מהירות למשך 10 שניות", 20);
    }

    public boolean rightClickAirAction(Player player, ItemStack itemStack) {
        if (!this.getCommandCooldown().call(player)) return false;
        int time = 20 * 10;
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, time, 1, false, false));
        SoundUtils.playSoundForAll(player, Sound.ENTITY_WANDERING_TRADER_DRINK_POTION);
        return true;
    }

    public boolean rightClickBlockAction(Player player, ItemStack itemStack) { return this.rightClickAirAction(player, itemStack); }
    public boolean rightClickPlayerAction(Player player, Player player1, ItemStack itemStack) { return false; }
}
