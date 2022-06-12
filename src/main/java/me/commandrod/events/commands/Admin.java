package me.commandrod.events.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import me.commandrod.commandapi.utils.ConfigUtils;
import me.commandrod.commandapi.utils.MessageUtils;
import me.commandrod.commandapi.utils.Utils;
import me.commandrod.events.Main;
import me.commandrod.events.api.event.Event;
import me.commandrod.events.api.event.EventManager;
import me.commandrod.events.api.event.EventType;
import me.commandrod.events.events.Simon;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Admin {

    @CommandMethod("commandevents <option>")
    @CommandPermission("active.admin")
    @CommandDescription("Main command for admins.")
    public void commandEvents(CommandSender sender, @Argument("option") String[] option) {
        if (option.length == 0) return;
        switch (option[0].toLowerCase()) {
            case "reloadconfig" -> {
                ConfigUtils.getInstance().reloadConfig();
                ConfigUtils.getInstance().saveConfig();
                sender.sendMessage(Utils.color("&3Successfully reloaded the configuration file."));
            }
            case "pvp" -> {
                if (!EventManager.isEventRunning()) break;
                Event event = Main.getEvent();
                if (!event.getType().equals(EventType.SIMON)) break;
                Simon sEvent = (Simon) event;
                boolean newPvP = !sEvent.isPvP();
                sender.sendMessage(Utils.color("&cChanged pvp to &7" + newPvP + "&c."));
                sEvent.setPvP(newPvP);
            }
            case "setlocation" -> {
                if (!(sender instanceof Player p)) {
                    sender.sendMessage(MessageUtils.NOT_PLAYER);
                    return;
                }
                if (option.length == 1) {
                    p.sendMessage(Utils.color("&cYou must provide a location name!"));
                    return;
                }
                String locName = option[1];
                ConfigUtils.getInstance().setLocation(p.getLocation(), locName.toLowerCase());
                p.sendMessage(Utils.color("&3Successfully set the config location \"&b" + locName + "&3\"."));
            }
            default -> sender.sendMessage(Utils.color("""
                    &3===== &b&lAdmin Commands &3=====
                    &3 - reloadconfig &7- &bReloads the configuration file.
                    &3 - pvp &7- &bDisables/Enables pvp in the Simon Says gamemode.
                    &3 - setlocation &7- &bSets a location in the configuration file.
                    """));
        }
    }
}