package com.github.mwierzchowski.sun.management;

import com.github.mwierzchowski.sun.core.SunEphemerisProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Clock;

@Component
@RequiredArgsConstructor
public class SunEphemerisProviderHealth implements HealthIndicator {
    private final Clock clock;

    @EventListener
    public void onStatusUpdate(SunEphemerisProvider.Status status) {
        // todo
    }

    @Override
    public Health health() {
        return null;
    }
}
