package me.commandrod.events.listeners;

import lombok.Getter;
import me.commandrod.events.api.event.Event;
import me.commandrod.events.api.event.EventManager;
import org.bukkit.Bukkit;

@Getter
public class ActiveEffectListener implements Runnable {

    private final Event event;
    private final int time;

    public ActiveEffectListener(Event event){
        this.event = event;
        this.time = event.getActiveEffectTime();
    }

    @Override
    public void run() {
        if (!EventManager.isEventRunning()) return;
        event.activeEffect();
    }
}
