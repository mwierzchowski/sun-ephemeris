package com.github.mwierzchowski.sun.management;

import com.github.mwierzchowski.sun.core.SunEphemeris;
import com.github.mwierzchowski.sun.core.SunEvent;
import com.github.mwierzchowski.sun.core.SunEventPublishScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.LinkedHashMap;

import static java.util.stream.Collectors.toMap;

@Component
@RequiredArgsConstructor
public class SunEphemerisInfo implements InfoContributor {
    public static final String ROOT_KEY = "todayEphemeris";
    public static final String NOT_AVAILABLE = "n/a";

    private final Clock clock;
    private SunEphemeris ephemeris;

    @Async
    @EventListener
    public synchronized void onSuccess(SunEventPublishScheduler.SuccessEvent success) {
        ephemeris = (SunEphemeris) success.getSource();
    }

    @Async
    @EventListener
    public synchronized void onFailure(SunEventPublishScheduler.FailureEvent failure) {
        ephemeris = null;
    }

    @Override
    public synchronized void contribute(Info.Builder builder) {
        Object details = NOT_AVAILABLE;
        if (ephemeris != null && ephemeris.isToday(clock)) {
            details = ephemeris.stream().collect(toMap(SunEvent::getName, event -> event.getLocalTime(clock), (o1, o2) -> o1, LinkedHashMap::new));
        }
        builder.withDetail(ROOT_KEY, details);
    }
}
