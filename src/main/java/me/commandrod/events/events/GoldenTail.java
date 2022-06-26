package me.commandrod.events.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.commandrod.commandapi.cooldown.CommandCooldown;
import me.commandrod.commandapi.items.ItemUtils;
import me.commandrod.commandapi.utils.Utils;
import me.commandrod.events.Main;
import me.commandrod.events.api.Counter;
import me.commandrod.events.api.event.EventState;
import me.commandrod.events.api.event.EventType;
import me.commandrod.events.api.event.Handle;
import me.commandrod.events.api.event.template.RoundEvent;
import me.commandrod.events.utils.EventUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class GoldenTail extends RoundEvent {

    private final List<Player> taggers;
    private final CommandCooldown cooldownManager;
    private final Counter counter;

    public GoldenTail() {
        super(EventType.GOLDENTAIL, "מכנסי הזהב", 45);
        this.taggers = new ArrayList<>();
        this.cooldownManager = new CommandCooldown(1, false);
        this.counter = new Counter("Total Points");
    }

    public List<String> getExtraLines(Player player) {
        return Arrays.asList(
                "&7מספר הבורחים: &b" + this.taggers.size(),
                "&7נקודות: " + filterPoints(player)
        );
    }

    public void preEventStart() {
        for (Player player : this.getPlayers())
            CountPlayer.from(player);
    }

    public void timeRunning() {
        for (Player player : this.taggers) {
            CountPlayer p = CountPlayer.from(player);
            p.add();
        }
    }

    public void timeOver() {
        for (CountPlayer player : this.getEliminatedPlayers())
            this.eliminate(player.getPlayer());
        this.randomTag();
    }

    public boolean onDamageByPlayer(Player attacker, Player damaged) {
        if (!this.taggers.contains(damaged)) return true;
        if (this.taggers.contains(attacker)) return true;
        if (!this.cooldownManager.call(damaged) || !this.cooldownManager.call(attacker)) return true;
        this.tag(attacker);
        this.untag(damaged);
        return true;
    }

     public void onDeath(Player player) {
        this.untag(player);
    }

    public void onEventStart() { this.randomTag(); }
    public void onEventEnd(Player winner) { this.taggers.clear(); }
    public Handle onInventoryClick(Player clicker, InventoryClickEvent event) { return Handle.TRUE; }

    private void randomTag() {
        if (!this.getEventState().equals(EventState.PLAYING)) return;
        for (Player tagger : this.taggers)
            this.untag(tagger);
        for (CountPlayer player : CountPlayer.players)
            this.counter.add(player.getPlayer(), player.getScore());

        CountPlayer.playerMap.clear();
        CountPlayer.players.clear();

        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            for (int i = 0; i < this.getAmount(); i++){
                List<Player> filtered = this.getPlayers()
                        .stream()
                        .filter(Predicate.not(this.taggers::contains))
                        .collect(Collectors.toList());
                Player player = filtered.get(EventUtils.random(filtered.size()));
                this.tag(player);
            }
            this.resetTime();
        }, 50);
    }

    private void untag(Player player) {
        this.taggers.remove(player);
        player.getInventory().setLeggings(null);
        player.setGlowing(false);
        this.changeName(player, "&f");
    }

    private void tag(Player player) {
        if (this.taggers.contains(player)) return;
        this.taggers.add(player);
        player.getInventory().setLeggings(leggings());
        player.setGlowing(true);
        this.changeName(player, "&e");
    }

    private ItemStack leggings() {
        return ItemUtils.quickItem(Material.GOLDEN_LEGGINGS, "&6אתה הבורח!",
                Collections.singletonList("נסה להשאר כמה שיותר עם המכנסיים!&7"), false);
    }

    private void changeName(Player player, String color) {
        player.playerListName(Utils.color(color + player.getName()));
    }

    private String filterPoints(Player player) {
        CountPlayer p = CountPlayer.from(player);
        List<CountPlayer> players = this.getPassedPlayers();
        String color = players.contains(p) ? "&a" : "&c";
        return color + p.getScore();
    }

    private List<CountPlayer> getEliminatedPlayers() {
        List<CountPlayer> players = CountPlayer.getSortedPlayers();
        return players.subList(0, players.size() - this.getAmount());
    }

    private List<CountPlayer> getPassedPlayers() {
        List<CountPlayer> list = CountPlayer.players;
        list.removeAll(this.getEliminatedPlayers());
        return list;
    }

    @AllArgsConstructor
    @Getter
    private static class CountPlayer {

        private final Player player;
        private int score;

        private static final HashMap<Player, CountPlayer> playerMap = new HashMap<>();
        private static final List<CountPlayer> players = new ArrayList<>();

        public static CountPlayer from(Player player) {
            if (playerMap.containsKey(player)) {
                return playerMap.get(player);
            }
            CountPlayer countPlayer = new CountPlayer(player, 0);
            playerMap.put(player, countPlayer);
            players.add(countPlayer);
            return countPlayer;
        }

        public static List<CountPlayer> getSortedPlayers() {
            return players
                    .stream()
                    .sorted(Comparator.comparing(CountPlayer::getScore))
                    .collect(Collectors.toList());
        }

        public void add() {
            this.score++;
        }
    }
}
