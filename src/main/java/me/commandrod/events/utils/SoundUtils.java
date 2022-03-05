package me.commandrod.events.utils;

import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

public class SoundUtils {

    public static void playSound(Player player, Sound sound){
        player.playSound(player.getLocation(), sound, SoundCategory.MASTER, 1, 1);
    }
    public static void playSound(Player player, Sound sound, int pitch){
        player.playSound(player.getLocation(), sound, SoundCategory.MASTER, 1, pitch);
    }
}