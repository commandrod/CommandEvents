package me.commandrod.events.api.event;

import lombok.Getter;
import lombok.Setter;
import me.commandrod.commandapi.other.RepeatingTask;
import me.commandrod.commandapi.utils.ConfigUtils;
import me.commandrod.commandapi.utils.MessageUtils;
import me.commandrod.commandapi.utils.SoundUtils;
import me.commandrod.commandapi.utils.Utils;
import me.commandrod.events.Main;
import me.commandrod.events.listeners.ActiveEffectListener;
import me.commandrod.events.utils.EventUtils;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Getter @Setter
public abstract class Event implements AbstractEvent {

    private final EventType type;
    private final List<Player> players;
    private final String subtitle, friendlyName, configName;
    private final int activeEffectTime;
    private final Event instance;

    private EventState eventState;

    private HashMap<NamedTextColor, Team> teams;

    public Event(EventType type, String friendlyName, int activeEffectTime) {
        this.type = type;
        this.players = new ArrayList<>();
        this.eventState = EventState.LOBBY;
        FileConfiguration config = Main.getPlugin().getConfig();
        this.configName = type.name().toLowerCase();
        String subtitlePath = "subtitles." + this.configName;
        this.subtitle = config.isSet(subtitlePath) ? config.getString(subtitlePath) : "Good luck!";
        this.friendlyName = friendlyName;
        this.activeEffectTime = activeEffectTime;
        this.instance = this;
        this.teams = new HashMap<>();
    }

    public Event(EventType type, String friendlyName) {
        this(type, friendlyName, 20);
    }

    public void spawn() {
        for (Player player : this.players) player.teleport(this.getSpawnLocation());
    }

    public void countdown(int seconds) {
        if (this.getSpawnLocation() == null){
            Bukkit.broadcast(Utils.color("&cAn error has occurred while trying to start this event!"), "commandevents.admin");
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.teleport(this.getSpawnLocation());
            if (player.getGameMode().equals(GameMode.CREATIVE) || player.getGameMode().equals(GameMode.SPECTATOR)) continue;
            this.players.add(player);
            this.sendScoreboard(player);
            player.setGameMode(this.getDefaultGamemode());
            this.color(player, NamedTextColor.WHITE);
            this.setup(player);
        }

        WorldBorder border = this.getSpawnLocation().getWorld().getWorldBorder();
        border.reset();
        this.preEventStart();

        AtomicInteger countdown = new AtomicInteger(seconds);
        this.eventState  = EventState.STARTING;
        new RepeatingTask(20) {
            final int stay = 40;
            final Event event = getInstance();
            public void run() {
                if (countdown.get() > 0){
                    for (Player player : event.players) {
                        if (3 >= countdown.get()) SoundUtils.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING);
                        player.showTitle(Utils.toTitle("&3Starting in " + countdown, "&b" + event.getSubtitle(), 0, stay, 0));
                    }
                }
                if (countdown.get() == 0){
                    for (Player player : event.players){
                        SoundUtils.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 2f, 1f);
                        player.showTitle(Utils.toTitle("&3Good luck!", "&bTry to be the last one standing!", 0, stay, 20));
                    }
                    event.setEventState(EventState.PLAYING);
                    event.onEventStart();
                    ActiveEffectListener listener = new ActiveEffectListener(event);
                    int time = listener.getTime();
                    if (time > 0) {
                        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), listener, 0, time);
                    }
                    Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(),
                            () -> Bukkit.getOnlinePlayers().forEach(event::sendScoreboard), 0, 20);
                    this.cancel();
                }
                countdown.getAndDecrement();
            }
        };
    }

    public void eliminate(Player player){
        player.playerListName(Utils.color("&c" + player.getName()));
        player.setGameMode(GameMode.SPECTATOR);
        if (this.isDead(player)) return;
        player.showTitle(Utils.toTitle("&cYou died!", "", 10, 60, 10));
        this.players.remove(player);
        this.onDeath(player);
        player.teleport(this.getSpawnLocation());
        this.players.forEach(this::sendScoreboard);
        this.end();
        if (player.getKiller() == null){
            Bukkit.broadcast(Utils.color("&b" + player.getName() + " &fhas been eliminated."));
            return;
        }
        Bukkit.broadcast(Utils.color("&b" + player.getName() + " &fhas been eliminated by &b" + player.getKiller().getName() + "&f."));
    }

    public void revive(Player player) {
        this.onRespawn(player);
        this.setup(player);
        this.players.add(player);
        player.teleport(this.getSpawnLocation());
        player.setGameMode(this.getDefaultGamemode());
        EventUtils.heal(player, false);
        for (Player players : this.players)
            this.sendScoreboard(players);
    }

    public void lobby() {
        this.setEventState(EventState.LOBBY);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            EventUtils.heal(player, true);
            player.setGameMode(GameMode.SURVIVAL);
            Optional<Location> opSpawn = ConfigUtils.getInstance().getLocation("spawn");
            opSpawn.ifPresentOrElse(player::teleport,
                    () -> player.sendMessage(Utils.color(MessageUtils.ERROR_ADMIN)));
        }
    }

    public void stop() {
        this.lobby();
        Bukkit.getScheduler().cancelTasks(Main.getPlugin());
    }

    public void end() {
        if (this.players.size() != 1) return;
        this.winner(this.players.get(0));
    }

    public void winner(Player winner) {
        if (!this.getEventState().equals(EventState.PLAYING)) return;
        this.setEventState(EventState.ENDING);
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            Bukkit.broadcast(Utils.color("&3Winner: &b\n" + winner.getName()), "commandevents.admin");
            this.lobby();
            this.onEventEnd(winner);
            Optional<Location> opWinner = ConfigUtils.getInstance().getLocation("winner-location");
            opWinner.ifPresentOrElse(winner::teleport,
                    () -> winner.sendMessage(Utils.color(MessageUtils.ERROR_ADMIN)));

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.showTitle(Utils.toTitle("&b" + winner.getName() + " won!", "&3GG", 10, 80, 10));
                if (player == winner) continue;
                Optional<Location> opWinnerView = ConfigUtils.getInstance().getLocation("winner-view-location");
                opWinnerView.ifPresentOrElse(player::teleport,
                        () -> player.sendMessage(Utils.color(MessageUtils.ERROR_ADMIN)));
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

    public void color(Player player, NamedTextColor color) {
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team;
        if (!this.teams.containsKey(color)) {
            String name = color.toString().toLowerCase();
            team = sb.getTeam(name);
            if (team == null) {
                team = sb.registerNewTeam(name);
                team.color(color);
            }
        } else {
            team = this.teams.get(color);
        }
        Team oldTeam = sb.getEntryTeam(player.getName());
        if (oldTeam != null) {
            oldTeam.removeEntry(player.getName());
        }
        team.addEntry(player.getName());
    }

    public void sendScoreboard(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = scoreboard.registerNewObjective("gamesb", "dummy", Utils.color("&b&lActive Events"));
        List<String> lines = new ArrayList<>();
        lines.add("&b" + this.getFriendlyName());
        lines.add("&7 ");
        if (this.getLines(player) != null) lines.addAll(this.getLines(player));
        lines.add("&7???????????? ????????????: &b" + this.players.size());
        lines.add("&e ");
        lines.add("&eActiveEvents.club");
        for (int i = 0; i < lines.size(); i++){
            String line = Utils.legacyColor(lines.get(i));
            obj.getScore(line).setScore(lines.size() - i);
        }
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        player.setScoreboard(scoreboard);
    }

    public boolean isDead(Player player) {
        return !this.players.contains(player);
    }

    public Location getSpawnLocation() {
        Optional<Location> opSpawn = ConfigUtils.getInstance().getLocation(this.configName + "-spawn-location");
        if (opSpawn.isEmpty()) {
            Main.getPlugin().getLogger().severe("Error getting spawn location for " + this.configName + "!");
            return null;
        }
        return opSpawn.get();
    }

    public GameMode getDefaultGamemode() { return GameMode.ADVENTURE; }

    public void setup(Player player) {}
    public List<String> getLines(Player player) { return null; }
    public void activeEffect() {}
    public void preEventStart() {}
    public void onEventStart() {}
    public void onEventEnd(Player winner) {}
    public void onDeath(Player player) {}
    public void onRespawn(Player player) {}
    public boolean onBreakBlock(BlockBreakEvent event, Player breaker, Block block) { return true; }
    public boolean onPlaceBlock(BlockPlaceEvent event, Player placer, Block block, Block replacedBlock) { return true; }
    public boolean onDamageByPlayer(Player attacker, Player damaged) { return true; }
    public boolean onDamage(Player player, EntityDamageEvent event) { return event.getCause().equals(EntityDamageEvent.DamageCause.FALL); }
    public Handle onInventoryClick(Player clicker, InventoryClickEvent event) { return Handle.NONE; }
    public Handle onInteract(Player player, PlayerInteractEvent event) { return Handle.NONE; }
}