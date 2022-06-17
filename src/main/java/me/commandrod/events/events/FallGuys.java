package me.commandrod.events.events;

import me.commandrod.events.api.event.Event;
import me.commandrod.events.api.event.EventType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.List;

public class FallGuys extends Event {

    public FallGuys() {
        super(EventType.FALLGUYS, "פול גאייז", -1);
    }

    public List<String> getLines(Player player) {
        return null;
    }

    public boolean onDamage(Player player, EntityDamageEvent event) { return event.getCause().equals(EntityDamageEvent.DamageCause.FALL); }
}
