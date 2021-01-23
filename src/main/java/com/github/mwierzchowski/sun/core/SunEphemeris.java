package com.github.mwierzchowski.sun.core;

import java.io.Serializable;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;

public class SunEphemeris implements Serializable {
    private SortedMap<SunEventType, Instant> events = new TreeMap<>();

    public void add(SunEventType type, OffsetDateTime offsetDateTime) {
        events.put(type, offsetDateTime.toInstant());
    }

    public Stream<SunEvent> eventStream() {
        return events.entrySet().stream().map(this::toSunEvent);
    }

    public SunEvent firstEvent() {
        return eventStream().findFirst().get();
    }

    public Optional<SunEvent> firstEventAfterNow() {
        var now = Instant.now();
        return eventStream().filter(event -> event.getTimestamp().isAfter(now)).findFirst();
    }

    private SunEvent toSunEvent(Map.Entry<SunEventType, Instant> entry) {
        var event = new SunEvent();
        event.setType(entry.getKey());
        event.setTimestamp(entry.getValue());
        return event;
    }
}
