package com.github.mwierzchowski.sun.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;

import static java.time.Instant.now;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SunEvent implements Serializable {
    private SunEventType type;
    private Instant timestamp;

    public boolean isStale(Clock clock) {
        return timestamp.isBefore(now(clock));
    }

    public LocalTime getLocalTime(Clock clock) {
        return timestamp.atZone(clock.getZone()).toLocalTime();
    }

    public String getName() {
        return type.description();
    }
}
