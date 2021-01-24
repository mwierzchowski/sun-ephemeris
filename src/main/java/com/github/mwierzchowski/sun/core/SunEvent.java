package com.github.mwierzchowski.sun.core;

import lombok.Data;

import java.io.Serializable;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;

import static java.time.Instant.now;

@Data
public class SunEvent implements Serializable {
    private SunEventType type;
    private Instant timestamp;

    public boolean isStale(Clock clock) {
        return timestamp.isBefore(now(clock));
    }

    public LocalDateTime getLocalDateTime(Clock clock) {
        return timestamp.atZone(clock.getZone()).toLocalDateTime();
    }
}
