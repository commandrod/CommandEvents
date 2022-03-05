package me.commandrod.events.commands;

import me.commandrod.events.Main;
import me.commandrod.events.api.event.Event;
import me.commandrod.events.api.event.EventManager;
import me.commandrod.events.api.event.EventType;
import me.commandrod.events.utils.MessageUtils;
import me.commandrod.events.utils.Utils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Start implements CommandExecutor, TabCompleter {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("start")){
            if (!sender.hasPermission("commandevents.start")){
                sender.sendMessage(MessageUtils.PERM);
                return true;
            }
            if (args.length == 0){
                sender.sendMessage(MessageUtils.cmdUsage(cmd));
                return true;
            }
            if (EventManager.isEventRunning()) {
                sender.sendMessage(Utils.color("&cThere is a running event!"));
                return true;
            }
            String eventName = args[0].toUpperCase();
            if (!EventManager.doesExist(eventName)){
                sender.sendMessage(Utils.color("&cThe provided event is invalid!"));
                return true;
            }
            EventType eventType = EventType.valueOf(eventName);
            Collection<Player> readyPlayers = Bukkit.getOnlinePlayers().stream().filter(player -> player.getGameMode().equals(GameMode.SURVIVAL) || player.getGameMode().equals(GameMode.ADVENTURE)).collect(Collectors.toList());
            if (readyPlayers.size() < 2){
                sender.sendMessage(Utils.color("&cThere are not enough online players!"));
                return true;
            }
            int seconds;
            if (args.length == 1){
                seconds = 15;
            } else {
                seconds = StringUtils.isNumeric(args[1]) ? Integer.parseInt(args[1]) : 15;
            }
            Main.setEvent(EventManager.getEvent(eventType));
            Event event = Main.getEvent();
            if (event.getSpawnLocation() == null){
                sender.sendMessage(Utils.color("&cThe start location of this event is invalid!"));
                return true;
            }
            event.countdown(seconds);
            sender.sendMessage(Utils.color("&3Successfully started the event &b" + eventType.name() + "&3."));
        }
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (cmd.getName().equalsIgnoreCase("start")) {
            if (!sender.hasPermission("commandevents.start")) return Collections.EMPTY_LIST;
            final List<String> oneArgList = new ArrayList<>();
            final List<String> completions = new ArrayList<>();
            for (EventType type : EventType.values()){
                if (EventManager.getEvents().containsKey(type)) oneArgList.add(type.name().toUpperCase());
            }
            if (args.length == 1){
                StringUtil.copyPartialMatches(args[0], oneArgList, completions);
            }
            Collections.sort(completions);
            return completions;
        }
        return Collections.EMPTY_LIST;
    }
}