package me.commandrod.events.utils;

import me.commandrod.events.api.event.Event;
import org.bukkit.command.Command;

public class MessageUtils {

    public static final String NOT_PLAYER = Utils.color("&c[CommandEvents] This command may only be executed by a player!");
    public static final String PERM = Utils.color("&cInsufficient permissions.");
    public static String cmdUsage(Command command){
        return Utils.color("&b" + command.getDescription() + "&3\n" + command.getUsage().replace("<command>", command.getName()));
    }

    public static String border(Event event) {
        int ogSize = (int) event.getSpawnLocation().getWorld().getWorldBorder().getSize();
        int size = ogSize > 10000 ? 200 : ogSize;
        return "&7בורדר: &b" + size;
    }
}