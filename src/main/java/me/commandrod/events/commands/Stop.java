package me.commandrod.events.commands;

import me.commandrod.commandapi.utils.MessageUtils;
import me.commandrod.commandapi.utils.Utils;
import me.commandrod.events.Main;
import me.commandrod.events.api.event.Event;
import me.commandrod.events.api.event.EventManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Stop implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("stopgm")){
            if (!(sender instanceof Player)){
                sender.sendMessage(MessageUtils.NOT_PLAYER);
                return true;
            }
            Player p = (Player) sender;
            if (!p.hasPermission("commandevents.stop")){
                p.sendMessage(MessageUtils.PERM);
                return true;
            }
            Event event = Main.getEvent();
            if (!EventManager.isEventRunning()) {
                p.sendMessage(Utils.color("&cThere are no running events!"));
                return true;
            }
            p.sendMessage(Utils.color("&3Successfully stopped the event &b" + event.getType().name() + "&3."));
            event.stop();
            Main.setEvent(null);
        }
        return true;
    }
}