package me.commandrod.events.events;

import lombok.Getter;
import me.commandrod.commandapi.utils.Utils;
import me.commandrod.events.api.event.Event;
import me.commandrod.events.api.event.EventType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class ParkourTag extends Event {

    private final List<Player> taggers;
    private int time;

    private final int defaultTime = 30;

    public ParkourTag() {
        super(EventType.PARKOURTAG, "תופסת הפארקור");
        this.taggers = new ArrayList<>();
        this.time = 30;
    }

    public List<String> getLines(Player player) {
        String role = roleFilter(player);
        return Arrays.asList(
                "&7זמן נותר: &b" + this.time,
                "&7תפקיד: " + role
        );
    }

    public void activeEffect() {
        if (this.time > 0) this.time--;
        if (this.time != 0) return;
        for (Player tagger : this.taggers) {
            int aliveRunners = (int) Game.from(tagger).runners.stream()
                    .filter(this::isDead).count();
            if (aliveRunners == 0) continue;
            this.eliminate(tagger);
        }
    }

    public void setup(Player player) {
        this.sendRole(player);
    }

    public void onEventStart() { this.initPlayers(); }

    private void initPlayers() {
        int taggersAmount = Math.max(Math.floorDiv(this.getPlayers().size(), 4), 1);
        for (int i = 0; i < taggersAmount; i++) {
            List<Player> filtered = this.getPlayers().stream()
                    .filter(p -> !this.taggers.contains(p))
                    .collect(Collectors.toList());
            int bound = this.getPlayers().size() - 1;
            Player player = filtered.get(ThreadLocalRandom.current().nextInt(bound));
            Game game = this.setRunner(player);
            int runnerAmount = Math.min(filtered.size(), 3);
            for (int a = 0; a < runnerAmount; a++) {
                List<Player> filteredRunners = this.getPlayers().stream()
                        .filter(p -> !Game.getAllPlayers().contains(p))
                        .collect(Collectors.toList());
                Player runner = filteredRunners.get(ThreadLocalRandom.current().nextInt(bound));
                game.addRunner(runner);
            }
        }
    }

    private Game setRunner(Player player) {
        this.taggers.add(player);
        return Game.from(player);
    }

    private void sendRole(Player player) {
        boolean tagger = this.taggers.contains(player);
        String role = roleFilter(player);
        String job = tagger ? "לתפוס את הרצים עד שהזמן נגמר!" : "להתחמק מהתופס עד שהזמן נגמר!";
        Bukkit.broadcast(Utils.color("&3" + player.getName() + "&7: " + role));
        player.showTitle(Utils.toTitle("&fאתה " + role, "&bעליך " + job, 10, 120, 10));
    }

    private String roleFilter(Player player) {
        return taggers.contains(player) ? "&cתופס!" : "&bרץ!";
    }

    private record Game(@Getter Player tagger, @Getter List<Player> runners) {

        private static final HashMap<Player, Game> games = new HashMap<>();
        @Getter
        private static final List<Player> allPlayers = new ArrayList<>();

        public void addRunner(Player player) {
            allPlayers.add(player);
            this.runners.add(player);
        }

        public static Game from(Player tagger) {
            if (!games.containsKey(tagger)) {
                allPlayers.add(tagger);
                Game game = new Game(tagger, new ArrayList<>());
                games.put(tagger, game);
                return game;
            }
            return games.get(tagger);
        }
    }
}
