package me.commandrod.events.events;

import me.commandrod.commandapi.cooldown.CommandCooldown;
import me.commandrod.commandapi.items.ItemUtils;
import me.commandrod.events.Main;
import me.commandrod.events.api.Counter;
import me.commandrod.events.api.event.Event;
import me.commandrod.events.api.event.EventState;
import me.commandrod.events.api.event.EventType;
import me.commandrod.events.api.event.Handle;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class GoldenTail extends Event {

    private final List<Player> taggers;
    private final CommandCooldown cooldownManager;
    private final Counter counter;
    private int time;
    private int round;

    private final int defaultTime = 45;

    public GoldenTail() {
        super(EventType.GOLDENTAIL, "מכנסי הזהב");
        this.taggers = new ArrayList<>();
        this.cooldownManager = new CommandCooldown(.3f, false);
        this.counter = new Counter("Points");
        this.time = defaultTime;
        this.round = 0;
    }

    public List<String> getLines(Player player) {
        return Arrays.asList(
                "&7סיבוב מספר: &b" + this.round,
                "&7זמן לסיבוב: &b" + this.time,
                "&7מספר התופסים: &b" + this.taggers.size()
        );
    }

    // Time Manager
    public void activeEffect() {
        if (this.time > 0) {
            for (Player player : this.taggers)
                this.counter.add(player);
            this.time--;
            return;
        }
//        Set<Map.Entry<UUID, Integer>> playerCounterMap = this.sortByValue(this.counter.getMap()).entrySet();
//        List<Player> filteredPlayers = new ArrayList<>();
//        for (int i = 0; i < this.getAmount(); i++) {
//            playerCounterMap
//            filteredPlayers.add();
//        }
//        for (Player player : filteredPlayers)
//            this.eliminate(player);
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
        if (!this.taggers.contains(player)) return;
        this.untag(player);
        player.getLocation().createExplosion(4f, false, false);
    }

    public void onEventStart() { this.randomTag(); }
    public void onEventEnd(Player winner) { this.taggers.clear(); }
    public Handle onInventoryClick(Player clicker, InventoryClickEvent event) { return Handle.TRUE; }

    private HashMap<UUID, Integer> sortByValue(HashMap<UUID, Integer> hm) {
        List<Map.Entry<UUID, Integer> > list = new LinkedList<>(hm.entrySet());
        list.sort(Map.Entry.comparingByValue());
        HashMap<UUID, Integer> temp = new LinkedHashMap<>();
        for (Map.Entry<UUID, Integer> aa : list)
            temp.put(aa.getKey(), aa.getValue());
        return temp;
    }

    private void randomTag() {
        if (!this.getEventState().equals(EventState.PLAYING)) return;
        this.time = this.defaultTime;
        this.round++;
        int amount = this.getAmount();
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            for (int i = 0; i < amount; i++){
                List<Player> filtered = this.getPlayers().stream()
                        .filter(p -> !this.taggers.contains(p))
                        .collect(Collectors.toList());
                Player player = filtered.get(ThreadLocalRandom.current().nextInt(this.getPlayers().size() - 1));
                this.tag(player);
            }}, 50);
    }

    private int getAmount() {
        return Math.max(Math.floorDiv(this.getPlayers().size(), 3), 1);
    }

    private void untag(Player player) {
        this.taggers.remove(player);
        player.getInventory().setHelmet(null);
        player.setGlowing(false);
    }

    private void tag(Player player) {
        if (this.taggers.contains(player)) return;
        this.taggers.add(player);
        player.getInventory().setHelmet(helmet());
        player.setGlowing(true);
    }

    private ItemStack helmet() {
        return ItemUtils.quickItem(Material.GOLDEN_LEGGINGS, "&6אתה הבורח!",
                Collections.singletonList("נסה להשאר כמה שיותר עם המכנסיים!&6"), false);
    }
}
