package me.commandrod.events.commands;

import me.commandrod.commandapi.utils.MessageUtils;
import me.commandrod.commandapi.utils.Utils;
import me.commandrod.events.utils.ConfigUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetLocation implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("setlocation")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(MessageUtils.NOT_PLAYER);
                return true;
            }
            Player p = (Player) sender;
            if (!p.hasPermission("commandevents.setlocation")) {
                p.sendMessage(MessageUtils.PERM);
                return true;
            }
            if (args.length == 0) {
                MessageUtils.cmdUsage(cmd, sender);
                return true;
            }
            ConfigUtils.setLocation(p.getLocation(), args[0].toLowerCase());
            p.sendMessage(Utils.color("&3Successfully set the config location \"" + args[0] + "\"."));
        }
        return true;
    }
}