package me.commandrod.events.api.event.template;

import me.commandrod.events.api.event.Event;
import me.commandrod.events.api.event.EventType;
import me.commandrod.events.api.event.Handle;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;

public abstract class RoundEvent extends Event {

    private int time, round;
    private final int DEFAULT_TIME;

    public RoundEvent(EventType type, String friendlyName, int defaultTime) {
        super(type, friendlyName);
        this.time = defaultTime;
        this.round = 1;
        this.DEFAULT_TIME = defaultTime;
    }

    public List<String> getLines(Player player) {
        List<String> lines = new ArrayList<>();
        lines.add("&7סיבוב: &b" + this.round);
        lines.add("&7הריגה בעוד: &b" + this.time);
        if (this.getExtraLines(player) != null) lines.addAll(this.getExtraLines(player));
        return lines;
    }

    public void activeEffect() {
        if (this.time > 0) {
            this.timeRunning();
            this.time--;
            return;
        }
        this.timeOver();
    }

    public boolean onDamage(Player player, EntityDamageEvent event) { return true; }
    public Handle onInventoryClick(Player clicker, InventoryClickEvent event) { return Handle.TRUE; }

    public void resetTime() {
        this.time = this.DEFAULT_TIME;
        this.round++;
    }

    public int getAmount() {
        return Math.max(Math.floorDiv(this.getPlayers().size(), 3), 1);
    }

    public abstract void timeOver();
    public abstract void timeRunning();
    public abstract List<String> getExtraLines(Player player);
}
