package me.commandrod.events.api;

import lombok.Getter;
import me.commandrod.commandapi.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

@Getter
public class Counter {

    private final HashMap<UUID, Integer> map;
    private final String friendlyName;

    public Counter(String friendlyName){
        this.map = new HashMap<>();
        this.friendlyName = friendlyName;
    }

    public int getValue(Player player){
        if (!this.getMap().containsKey(player.getUniqueId())) this.getMap().put(player.getUniqueId(), 0);
        return this.getMap().get(player.getUniqueId());
    }

    public void set(Player player, int amount){
        if (this.getMap().containsKey(player.getUniqueId())){
            this.getMap().replace(player.getUniqueId(), amount);
            return;
        }
        this.getMap().put(player.getUniqueId(), amount);
    }

    public void add(Player player){ set(player, this.getValue(player) + 1); }

    public void printResults() {
        Bukkit.broadcast(Utils.color("&3" + this.getFriendlyName() + " Counter&7: "), "commandevents.admin");
        for (UUID uuid : this.getMap().keySet()){
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;
            int value = this.getValue(player);
            if (value == 0) continue;
            Bukkit.broadcast(Utils.color("&3 - " + player.getName() + "&7: &b" + value), "commandevents.admin");
        }
    }
}
