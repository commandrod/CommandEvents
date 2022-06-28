package me.commandrod.events;

import cloud.commandframework.annotations.AnnotationParser;
import lombok.Getter;
import lombok.Setter;
import me.commandrod.commandapi.CommandAPI;
import me.commandrod.commandapi.utils.ConfigUtils;
import me.commandrod.commandapi.utils.MessageUtils;
import me.commandrod.commandapi.utils.Utils;
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
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.reflections.Reflections;

import java.util.Optional;

public final class Main extends JavaPlugin {

    @Getter
    private CommandAPI api;
    @Getter
    private static Main plugin;
    @Getter @Setter
    private static Event event;

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
        for (Class<? extends Event> event : events.getSubTypesOf(Event.class)) {
            Event instance = null;
            try {
                instance = event.getDeclaredConstructor().newInstance();
            } catch (Exception ignored) { }
            if (instance == null) continue;
            try {
                EventManager.getEvents().put((EventType) event.getMethod("getType").invoke(instance), instance);
            } catch (Exception ignored) { }
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

        final Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        for (Player player : Bukkit.getOnlinePlayers()) {
            Optional<Location> opSpawn = ConfigUtils.getInstance().getLocation("spawn");
            opSpawn.ifPresentOrElse(player::teleport,
                    () -> player.sendMessage(Utils.color(MessageUtils.ERROR_ADMIN)));
            final String entry = player.getName();
            final Team team = sb.getEntryTeam(entry);
            if (team == null) continue;
            team.removeEntry(entry);
        }
    }
}
