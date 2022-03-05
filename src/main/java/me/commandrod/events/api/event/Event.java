package me.commandrod.events.api.event;

import lombok.Getter;
import lombok.Setter;
import me.commandrod.events.Main;
import me.commandrod.events.api.RepeatingTask;
import me.commandrod.events.listeners.ActiveEffectListener;
import me.commandrod.events.utils.ConfigUtils;
import me.commandrod.events.utils.SoundUtils;
import me.commandrod.events.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
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

    public Event(EventType type, String subtitle, String friendlyName, int activeEffectTime){
        this.type = type;
        this.players = new ArrayList<>();
        this.eventState = EventState.LOBBY;
        this.subtitle = subtitle;
        this.friendlyName = friendlyName;
        this.activeEffectTime = activeEffectTime;
        this.spawnLocation = ConfigUtils.getLocation(type.name().toLowerCase() + "-spawn-location");
        this.instance = this;
    }

    public Event(EventType type, String subtitle, String friendlyName){
        this(type, subtitle, friendlyName, 20);
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
            final Event event = getInstance();
            public void run() {
                if (countdown.get() > 0){
                    for (Player player : event.getPlayers()) {
                        if (3 >= countdown.get()) SoundUtils.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING);
                        player.sendTitle(Utils.color("&3Starting in " + countdown), Utils.color("&b" + event.getSubtitle()), 20, 20, 20);
                    }                }
                if (countdown.get() == 0){
                    for (Player player : event.getPlayers()){
                        SoundUtils.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 2);
                        player.sendTitle(Utils.color("&3Good luck!"), Utils.color("&bTry to be the last one standing!"), 20, 80, 20);
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
        player.sendTitle(Utils.color("&cYou died!"), "", 10, 60, 10);
        player.teleport(this.getSpawnLocation());
        this.getPlayers().remove(player);
        this.onDeath(player);
        this.getPlayers().forEach(this::sendScoreboard);
        this.end();
        if (player.getKiller() == null){
            Bukkit.broadcastMessage(Utils.color("&c" + player.getName() + " has been eliminated."));
            return;
        }
        Bukkit.broadcastMessage(Utils.color("&c" + player.getName() + " has been killed by " + player.getKiller().getName() + "."));
    }

    public void lobby(){
        this.setEventState(EventState.LOBBY);
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            Utils.heal(player, true);
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
                player.sendTitle(Utils.color("&b" + winner.getName() + " won!"), Utils.color("&3GG"), 10, 80, 10);
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
            String line = Utils.color(lines.get(i));
            obj.getScore(line).setScore(lines.size() - i);
        }
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        this.onScoreboardUpdate(scoreboard, player);
        player.setScoreboard(scoreboard);
    }

    public abstract List<String> getLines(Player player);
    public abstract void activeEffect();
    public abstract void preEventStart();
    public abstract void onEventStart();
    public abstract void onEventEnd(Player winner);
    public abstract void onDeath(Player player);
    public abstract void onRespawn(Player player);
    public abstract void onScoreboardUpdate(Scoreboard scoreboard, Player player);
    public abstract boolean onBreakBlock(BlockBreakEvent event, Player breaker, Block block);
    public abstract boolean onPlaceBlock(BlockPlaceEvent event, Player placer, Block block, Block replacedBlock);
    public abstract boolean onDamageByPlayer(Player attacker, Player damaged);
    public abstract boolean onDamage(Player player, EntityDamageEvent event);
    public abstract boolean onInventoryClick(Player clicker, InventoryClickEvent event);
}