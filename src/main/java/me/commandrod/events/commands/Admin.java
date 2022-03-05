package me.commandrod.events.commands;

import me.commandrod.events.Main;
import me.commandrod.events.utils.MessageUtils;
import me.commandrod.events.utils.Utils;
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
                sender.sendMessage(MessageUtils.cmdUsage(cmd));
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
                default:
                    sender.sendMessage(MessageUtils.cmdUsage(cmd));
                    break;
            }
        }
        return true;
    }
}