package me.commandrod.events;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import me.commandrod.commandapi.CommandAPI;
import me.commandrod.commandapi.items.CommandItem;
import me.commandrod.events.api.event.Event;
import me.commandrod.events.api.event.EventManager;
import me.commandrod.events.api.event.EventType;
import me.commandrod.events.commands.Admin;
import me.commandrod.events.commands.SetLocation;
import me.commandrod.events.commands.Start;
import me.commandrod.events.commands.Stop;
import me.commandrod.events.customlisteners.FFAListener;
import me.commandrod.events.customlisteners.SpleefListener;
import me.commandrod.events.listeners.EventListener;
import me.commandrod.events.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

public final class Main extends JavaPlugin {

    @Getter
    private CommandAPI api;
    @Getter
    private static Main plugin;
    @Getter @Setter
    private static Event event;

    @SneakyThrows
    public void onEnable() {
        this.saveDefaultConfig();

        api = new CommandAPI(this);
        api.registerCommands();

        plugin = this;

        this.getCommand("setlocation").setExecutor(new SetLocation());
        this.getCommand("start").setExecutor(new Start());
        this.getCommand("start").setTabCompleter(new Start());
        this.getCommand("stopgm").setExecutor(new Stop());
        this.getCommand("commandevents").setExecutor(new Admin());

        for (Class<? extends Event> event : new Reflections("me.commandrod.events.events").getSubTypesOf(Event.class)){
            Event instance = event.getDeclaredConstructor().newInstance();
            EventManager.getEvents().put((EventType) event.getMethod("getType").invoke(instance), instance);
        }

        for (Class<? extends CommandItem> item : new Reflections("me.commandrod.events.items").getSubTypesOf(CommandItem.class)){
            try {
                item.getDeclaredConstructor().newInstance();
                Bukkit.getLogger().info(Utils.color("&aLoaded " + item.getSimpleName()));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) { }
        }

        // Custom Listeners
        Bukkit.getPluginManager().registerEvents(new SpleefListener(), this);
        Bukkit.getPluginManager().registerEvents(new FFAListener(), this);

        for (World world : Bukkit.getServer().getWorlds()){
            world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        }

        Bukkit.getPluginManager().registerEvents(new EventListener(), this);
    }
}