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

    public Stream<SunEvent> stream() {
        return events.entrySet().stream().map(this::toSunEvent);
    }

    public SunEvent firstEvent() {
        return stream().findFirst()
                .orElseThrow(() -> new RuntimeException("SunEphemeris does not have any events"));
    }

    public Optional<SunEvent> firstEventAfter(Instant start) {
        return stream().filter(event -> event.getTimestamp().isAfter(start)).findFirst();
    }

    private SunEvent toSunEvent(Map.Entry<SunEventType, Instant> entry) {
        var event = new SunEvent();
        event.setType(entry.getKey());
        event.setTimestamp(entry.getValue());
        return event;
    }
}
