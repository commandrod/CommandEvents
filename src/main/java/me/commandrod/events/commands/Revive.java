package me.commandrod.events.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import me.commandrod.commandapi.utils.Utils;
import me.commandrod.events.Main;
import me.commandrod.events.api.event.Event;
import me.commandrod.events.api.event.EventManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Revive {

    @CommandMethod("revive <player>")
    @CommandPermission("active.revive")
    @CommandDescription("Revives a specified player.")
    public void revive(CommandSender sender, @Argument("player") Player t) {
        if (!EventManager.isEventRunning()) {
            sender.sendMessage(Utils.color("&cThere are no running events!"));
            return;
        }
        Event event = Main.getEvent();
        if (!event.isDead(t)) {
            sender.sendMessage(Utils.color("&cThe specified player is already alive!"));
            return;
        }
        event.revive(t);
        sender.sendMessage(Utils.color("&3Successfully revived the player &b" + t.getName() + "&3."));
    }
}