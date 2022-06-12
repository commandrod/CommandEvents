package me.commandrod.events.commands;

import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import me.commandrod.commandapi.utils.Utils;
import me.commandrod.events.Main;
import me.commandrod.events.api.event.Event;
import me.commandrod.events.api.event.EventManager;
import org.bukkit.command.CommandSender;

public class Stop {

    @CommandMethod("stopgm")
    @CommandPermission("active.stop")
    @CommandDescription("Stops all running events.")
    public void stop(CommandSender sender) {
        Event event = Main.getEvent();
        if (!EventManager.isEventRunning()) {
            sender.sendMessage(Utils.color("&cThere are no running events!"));
            return;
        }
        sender.sendMessage(Utils.color("&3Successfully stopped the event &b" + event.getType().name() + "&3."));
        event.stop();
        Main.setEvent(null);
    }
}