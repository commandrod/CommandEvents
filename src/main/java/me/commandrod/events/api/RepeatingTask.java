package me.commandrod.events.api;

import me.commandrod.events.Main;
import org.bukkit.Bukkit;

public abstract class RepeatingTask implements Runnable {

    private final int taskId;

    public RepeatingTask(int ticks){
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), this, 0, ticks);
    }

    public void cancel() {
        Bukkit.getScheduler().cancelTask(taskId);
    }
}
