package com.github.mwierzchowski.sun.core;

import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;

import static com.github.mwierzchowski.sun.core.SunEventType.NOON;

@EqualsAndHashCode
public class SunEphemeris implements Serializable {
    private final SortedMap<SunEventType, Instant> events = new TreeMap<>();

    public void add(SunEventType type, OffsetDateTime offsetDateTime) {
        events.put(type, offsetDateTime.toInstant());
    }

    public Stream<SunEvent> stream() {
        return events.entrySet().stream()
                .map(entry -> new SunEvent(entry.getKey(), entry.getValue()));
    }

    public SunEvent firstEvent() {
        return stream().findFirst()
                .orElseThrow(() -> new RuntimeException("SunEphemeris does not have any events"));
    }

    public Optional<SunEvent> firstEventAfter(Instant start) {
        return stream().filter(event -> event.getTimestamp().isAfter(start)).findFirst();
    }

    public boolean isToday(Clock clock) {
        var today = LocalDate.now(clock);
        return events.get(NOON)
                .atZone(clock.getZone())
                .toLocalDate()
                .equals(today);
    }
}
