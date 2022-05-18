package me.commandrod.events.api.event;

import lombok.Getter;
import lombok.Setter;
import me.commandrod.commandapi.utils.SoundUtils;
import me.commandrod.commandapi.utils.Utils;
import me.commandrod.events.Main;
import me.commandrod.events.api.RepeatingTask;
import me.commandrod.events.listeners.ActiveEffectListener;
import me.commandrod.events.utils.ConfigUtils;
import me.commandrod.events.utils.EventUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Getter @Setter
public abstract class Event {

    private EventType type;
    private List<Player> players;
    private Location spawnLocation;
    private EventState eventState;
    private String subtitle;
    private String friendlyName;
    private int activeEffectTime;
    private Event instance;

    public Event(EventType type, String friendlyName, int activeEffectTime){
        this.type = type;
        this.players = new ArrayList<>();
        this.eventState = EventState.LOBBY;
        FileConfiguration config = Main.getPlugin().getConfig();
        String eventConfigName = type.name().toLowerCase();
        String subtitlePath = "subtitles." + eventConfigName;
        this.subtitle = config.isSet(subtitlePath) ? config.getString(subtitlePath) : "Good luck!";
        this.friendlyName = friendlyName;
        this.activeEffectTime = activeEffectTime;
        this.spawnLocation = ConfigUtils.getLocation(eventConfigName + "-spawn-location");
        this.instance = this;
    }

    public Event(EventType type, String friendlyName){
        this(type, friendlyName, 20);
    }

    public void spawn() { for (Player player : this.getPlayers()) player.teleport(this.getSpawnLocation()); }

    public void countdown(int seconds) {
        if (this.getSpawnLocation() == null){
            Bukkit.broadcast(Utils.color("&cAn error has occurred while trying to start this event!"), "commandevents.admin");
            return;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.teleport(this.getSpawnLocation());
            if (player.getGameMode().equals(GameMode.CREATIVE) || player.getGameMode().equals(GameMode.SPECTATOR)) continue;
            this.getPlayers().add(player);
            this.sendScoreboard(player);
            player.setGameMode(GameMode.ADVENTURE);
        }
        preEventStart();
        AtomicInteger countdown = new AtomicInteger(seconds);
        setEventState(EventState.STARTING);
        new RepeatingTask(20) {
            final int stay = 40;
            final Event event = getInstance();
            public void run() {
                if (countdown.get() > 0){
                    for (Player player : event.getPlayers()) {
                        if (3 >= countdown.get()) SoundUtils.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING);
                        player.showTitle(Utils.toTitle("&3Starting in " + countdown, "&b" + event.getSubtitle(), 0, stay, 0));
                    }                }
                if (countdown.get() == 0){
                    for (Player player : event.getPlayers()){
                        SoundUtils.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 2f, 1f);
                        player.showTitle(Utils.toTitle("&3Good luck!", "&bTry to be the last one standing!", 0, stay, 20));
                    }
                    setEventState(EventState.PLAYING);
                    onEventStart();
                    this.cancel();
                    ActiveEffectListener listener = new ActiveEffectListener(event);
                    Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), listener, 0, listener.getTime());
                }
                countdown.getAndDecrement();
            }
        };
    }

    public void eliminate(Player player){
        player.setGameMode(GameMode.SPECTATOR);
        if (this.isDead(player)) return;
        player.showTitle(Utils.toTitle("&cYou died!", "", 10, 60, 10));
        this.getPlayers().remove(player);
        this.onDeath(player);
        player.teleport(this.getSpawnLocation());
        this.getPlayers().forEach(this::sendScoreboard);
        this.end();
        if (player.getKiller() == null){
            Bukkit.broadcast(Utils.color("&c" + player.getName() + " has been eliminated."));
            return;
        }
        Bukkit.broadcast(Utils.color("&c" + player.getName() + " has been killed by " + player.getKiller().getName() + "."));
    }

    public void revive(Player player){
        this.getPlayers().add(player);
        player.sendMessage(Utils.color("&3You have been revived."));
        player.teleport(this.getSpawnLocation());
        player.setGameMode(GameMode.SURVIVAL);
        EventUtils.heal(player, false);
        for (Player players : this.getPlayers()) {
            this.sendScoreboard(players);
        }
    }

    public void lobby(){
        this.setEventState(EventState.LOBBY);
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            EventUtils.heal(player, true);
            player.setGameMode(GameMode.SURVIVAL);
            player.teleport(ConfigUtils.getLocation("spawn"));
        });
    }

    public void stop(){
        this.lobby();
        Bukkit.getScheduler().cancelTasks(Main.getPlugin());
    }

    public void end() {
        if (this.getPlayers().size() != 1) return;
        this.winner(this.getPlayers().get(0));
    }

    public void winner(Player winner) {
        if (!this.getEventState().equals(EventState.PLAYING)) return;
        this.setEventState(EventState.ENDING);
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            Bukkit.broadcast(Utils.color("&3Winner: &b\n" + winner.getName()), "commandevents.admin");
            this.lobby();
            this.onEventEnd(winner);
            winner.teleport(ConfigUtils.getLocation("winner-location"));

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.showTitle(Utils.toTitle("&b" + winner.getName() + " won!", "&3GG", 10, 80, 10));
                if (player == winner) continue;
                player.teleport(ConfigUtils.getLocation("winner-view-location"));
            }

            AtomicInteger count = new AtomicInteger(20);
            Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), () -> {
                if (count.get() <= 0){
                    this.lobby();
                    Main.setEvent(null);
                    Bukkit.getScheduler().cancelTasks(Main.getPlugin());
                }
                winner.getWorld().spawnEntity(winner.getLocation(), EntityType.FIREWORK);
                count.getAndDecrement();
            }, 0, 10);
        }, 20*3);
    }

    public boolean isDead(Player player) {
        return !this.getPlayers().contains(player);
    }

    public void sendScoreboard(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = scoreboard.registerNewObjective("gamesb", "dummy", Utils.color("&b&lActive Events"));
        List<String> lines = new ArrayList<>();
        lines.add("&b" + this.getFriendlyName());
        lines.add("&7 ");
        if (this.getLines(player) != null) lines.addAll(this.getLines(player));
        lines.add("&7שחקנים שנשארו: &b" + this.getPlayers().size());
        lines.add("&e ");
        lines.add("&eactiveevents.ml");
        for (int i = 0; i < lines.size(); i++){
            String line = Utils.legacyColor(lines.get(i));
            obj.getScore(line).setScore(lines.size() - i);
        }
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        player.setScoreboard(scoreboard);
    }

    public abstract List<String> getLines(Player player);
    public abstract void activeEffect();
    public abstract void preEventStart();
    public abstract void onEventStart();
    public abstract void onEventEnd(Player winner);
    public abstract void onDeath(Player player);
    public abstract void onRespawn(Player player);
    public abstract boolean onBreakBlock(BlockBreakEvent event, Player breaker, Block block);
    public abstract boolean onPlaceBlock(BlockPlaceEvent event, Player placer, Block block, Block replacedBlock);
    public abstract boolean onDamageByPlayer(Player attacker, Player damaged);
    public abstract boolean onDamage(Player player, EntityDamageEvent event);
    public abstract boolean onInventoryClick(Player clicker, InventoryClickEvent event);
}