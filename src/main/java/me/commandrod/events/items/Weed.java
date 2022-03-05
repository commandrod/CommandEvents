package me.commandrod.events.items;

import me.commandrod.commandapi.utils.SoundUtils;
import me.commandrod.events.items.template.VapePowerup;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Weed extends VapePowerup {

    public Weed(){
        super(Material.BAMBOO, "WEED", "וויד", "נותן לשחקן אפקט בחילה למשך 5 שניות בלחיצה ימנית", 15);
    }

    public boolean rightClickPlayerAction(Player player, Player player1, ItemStack itemStack) {
        if (!this.getCommandCooldown().call(player)) return false;
        int time = 20*5;
        player1.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, time+80, 0, false, false));
        player1.addPotionEffect(new PotionEffect(PotionEffectType.POISON, time, 1, false, false));
        player1.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, time, 0, false, false));
        Sound sound = Sound.ENTITY_SPIDER_HURT;
        SoundUtils.playSound(player1, sound);
        SoundUtils.playSound(player, sound, player1.getLocation());
        return true;
    }

    public boolean rightClickBlockAction(Player player, ItemStack itemStack) { return false; }
    public boolean rightClickAirAction(Player player, ItemStack itemStack) { return false; }
}
