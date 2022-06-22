package me.commandrod.events;

import cloud.commandframework.annotations.AnnotationParser;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import me.commandrod.commandapi.CommandAPI;
import me.commandrod.events.api.event.Event;
import me.commandrod.events.api.event.EventManager;
import me.commandrod.events.api.event.EventType;
import me.commandrod.events.commands.Admin;
import me.commandrod.events.commands.Revive;
import me.commandrod.events.commands.Start;
import me.commandrod.events.commands.Stop;
import me.commandrod.events.customlisteners.SnowDodgeListener;
import me.commandrod.events.customlisteners.SpleefListener;
import me.commandrod.events.listeners.EventListener;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;

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
        plugin = this;

        api = new CommandAPI(this, "active");
        api.registerItems("me.commandrod.events.items");
        api.registerCommands();

        AnnotationParser<CommandSender> annotationParser = api.setupAnnotationCommands();

//        annotationParser.parse(new Start());
        annotationParser.parse(new Stop());
        annotationParser.parse(new Admin());
        annotationParser.parse(new Revive());

        final Reflections events = new Reflections("me.commandrod.events.events");
        for (Class<? extends Event> event : events.getSubTypesOf(Event.class)){
            Event instance = event.getDeclaredConstructor().newInstance();
            EventManager.getEvents().put((EventType) event.getMethod("getType").invoke(instance), instance);
        }

        this.getCommand("start").setExecutor(new Start());
        // Custom Listeners
        Bukkit.getPluginManager().registerEvents(new SpleefListener(), this);
        Bukkit.getPluginManager().registerEvents(new SnowDodgeListener(), this);

        for (World world : Bukkit.getServer().getWorlds()){
            world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        }

        Bukkit.getPluginManager().registerEvents(new EventListener(), this);
    }
}