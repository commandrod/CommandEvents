package me.commandrod.events.events;

import me.commandrod.events.api.Counter;
import me.commandrod.events.api.event.Event;
import me.commandrod.events.api.event.EventType;
import me.commandrod.events.utils.EventUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class KingOfTheHill extends Event {

    private final Counter points = new Counter("Points");
    private final Counter kills = new Counter("kills");

    public KingOfTheHill() {
        super(EventType.KOTH, "מלך הגבעה");
    }

    public List<String> getLines(Player player) {
        return Arrays.asList(
                "&7נקודות: &b" + this.points.getValue(player),
                "&7הריגות: &b" + this.kills.getValue(player)
        );
    }

    public void activeEffect() {
        for (Player player : this.getPlayers()) {
            if (!EventUtils.blockUnderIs(player, Material.GOLD_BLOCK)) continue;
            this.points.add(player);
            player.sendExperienceChange(0, this.points.getValue(player));
        }
    }

    public void onDeath(Player player) {
        Player killer = player.getKiller();
        if (killer == null) return;
        this.kills.add(killer);
        this.revive(player);
    }
}
