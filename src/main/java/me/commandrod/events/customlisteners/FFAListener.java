package me.commandrod.events.customlisteners;

import me.commandrod.events.utils.ItemUtils;
import me.commandrod.events.utils.SoundUtils;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class FFAListener implements Listener {

    @EventHandler
    public void onClick(PlayerInteractEvent e){
        Player p = e.getPlayer();
        ItemStack i = e.getItem();
        if (i == null) return;
        if (!ItemUtils.getStringFromItem(i, "id").equals("head")) return;
        i.setAmount(i.getAmount() - 1);
        SoundUtils.playSound(p, Sound.ENTITY_PLAYER_BURP);
        p.setFoodLevel(Math.min(20, p.getFoodLevel() + 5));
        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 5*20, 0, true, true));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 5*20, 0, true, true));
    }
}
