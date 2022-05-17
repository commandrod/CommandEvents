package me.commandrod.events.commands;

import me.commandrod.commandapi.utils.MessageUtils;
import me.commandrod.commandapi.utils.Utils;
import me.commandrod.events.Main;
import me.commandrod.events.api.event.Event;
import me.commandrod.events.api.event.EventManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Revive implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("revive")){
            if (!sender.hasPermission("commandevents.revive")){
                sender.sendMessage(MessageUtils.PERM);
                return true;
            }
            if (args.length == 0){
                MessageUtils.cmdUsage(cmd, sender);
                return true;
            }
            Player t = Bukkit.getPlayer(args[0]);
            if (t == null || !t.isOnline()){
                sender.sendMessage(me.commandrod.commandapi.utils.MessageUtils.INVALID_PLAYER);
                return true;
            }
            if (!EventManager.isEventRunning()) {
                sender.sendMessage(Utils.color("&cThere are no running events!"));
                return true;
            }
            Event event = Main.getEvent();
            if (!event.isDead(t)) {
                sender.sendMessage(Utils.color("&cThe specified player is already alive!"));
                return true;
            }
            event.revive(t);
            sender.sendMessage(Utils.color("&3Successfully revived the player &b" + t.getName() + "&3."));
        }
        return true;
    }
}