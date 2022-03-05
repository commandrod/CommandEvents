package me.commandrod.events.api;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter @Setter
public class CooldownManager {

    private Map<UUID, Long> cooldownMap;
    private double time;

    public CooldownManager(double time){
        this.time = time;
        this.cooldownMap = new HashMap<>();
    }

    public boolean call(Player player){
        if (!isCooldownOver(player)) return false;
        setCooldown(player);
        return true;
    }

    private void addValue(UUID uuid, long value){
        if (cooldownMap.containsKey(uuid)) return;
        cooldownMap.put(uuid, value);
    }

    private long getCooldown(Player player){
        return cooldownMap.get(player.getUniqueId());
    }

    public void setCooldown(Player player){
        addValue(player.getUniqueId(), System.currentTimeMillis());
    }

    public boolean isCooldownOver(Player player){
        if (!cooldownMap.containsKey(player.getUniqueId())) return true;
        if ((System.currentTimeMillis() - getCooldown(player)) >= time * 1000L){
            cooldownMap.remove(player.getUniqueId());
            return true;
        } else {
            return false;
        }
    }

    public long getSecondsLeft(Player player){
        return (long) (time - ((System.currentTimeMillis() - getCooldown(player))/1000%60));
    }
}