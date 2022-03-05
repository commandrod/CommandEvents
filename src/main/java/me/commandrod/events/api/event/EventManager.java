package me.commandrod.events.api.event;

import lombok.Getter;
import lombok.SneakyThrows;
import me.commandrod.events.Main;

import java.util.HashMap;
import java.util.stream.Collectors;

public class EventManager {

    @Getter
    private static final HashMap<EventType, Event> events = new HashMap<>();

    @SneakyThrows
    public static Event getEvent(EventType type){
        return events.get(type).getClass().getDeclaredConstructor().newInstance();
    }

    public static boolean doesExist(String eventType){
        return EventManager.getEvents().keySet()
                .stream()
                .map(EventType::name)
                .collect(Collectors.toList())
                .contains(eventType.toUpperCase());
    }

    public static boolean isEventRunning(){
        Event event = Main.getEvent();
        if (event == null) return false;
        return !event.getEventState().equals(EventState.LOBBY);
        //return !(event.getEventState().equals(EventState.LOBBY) || event.getEventState().equals(EventState.ENDING));
    }
}
