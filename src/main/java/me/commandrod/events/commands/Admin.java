package me.commandrod.events.commands;

import me.commandrod.commandapi.utils.MessageUtils;
import me.commandrod.commandapi.utils.Utils;
import me.commandrod.events.Main;
import me.commandrod.events.api.event.Event;
import me.commandrod.events.api.event.EventManager;
import me.commandrod.events.api.event.EventType;
import me.commandrod.events.events.Simon;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Admin implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("commandevents")) {
            if (!sender.hasPermission("commandevents.admin")) {
                sender.sendMessage(MessageUtils.PERM);
                return true;
            }
            if (args.length == 0) {
                MessageUtils.cmdUsage(cmd, sender);
                return true;
            }
            switch (args[0].toLowerCase()) {
                case "reloadconfig":
                    Main.getPlugin().reloadConfig();
                    Main.getPlugin().saveConfig();
                    sender.sendMessage(Utils.color("&3Successfully reloaded the configuration file."));
                    break;
                case "help":
                    sender.sendMessage(Utils.color(" &3===== &b&lAdmin Commands &3=====\n" +
                            " &3 - reloadconfig &7- &bReloads the configuration file."));
                    break;
                case "pvp":
                    if (!EventManager.isEventRunning()) break;
                    Event event = Main.getEvent();
                    if (!event.getType().equals(EventType.SIMON)) break;
                    Simon sEvent = (Simon) event;
                    boolean newPvP = !sEvent.isPvP();
                    sender.sendMessage(Utils.color("&cChanged pvp to &7" + newPvP + "&c."));
                    sEvent.setPvP(newPvP);
                    break;
                default:
                    MessageUtils.cmdUsage(cmd, sender);
                    break;
            }
        }
        return true;
    }
}