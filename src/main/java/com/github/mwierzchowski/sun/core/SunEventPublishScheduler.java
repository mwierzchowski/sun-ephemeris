package com.github.mwierzchowski.sun.core;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Provider;
import java.time.Clock;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class SunEventPublishScheduler {
    private final SunEphemerisProvider ephemerisProvider;
    private final Provider<SunEventPublishTask> publisherProvider;
    private final TaskScheduler taskScheduler;
    private final Clock clock;

    @Scheduled(cron = "${sun-ephemeris.schedule-cron}")
    @Retry(name = "SunEventPublisher", fallbackMethod = "scheduleEventsFallback")
    public void scheduleEvents() {
        LOG.info("Scheduling today events publication");
        var today = LocalDate.now(clock);
        ephemerisProvider.sunEphemerisFor(today).stream()
                .forEach(this::scheduleEvent);
    }

    public void scheduleEventsFallback(Throwable throwable) {
        LOG.warn("Scheduling events publication based on today ephemeris failed, trying yesterday", throwable);
        var yesterday = LocalDate.now(clock).minusDays(1);
        ephemerisProvider.sunEphemerisFor(yesterday).stream()
                .map(old -> new SunEvent(old.getType(), old.getTimestamp().plusSeconds(24L * 60 * 60)))
                .forEach(this::scheduleEvent);
    }

    private void scheduleEvent(SunEvent event) {
        var eventTime = event.getLocalDateTime(clock).toLocalTime();
        if (event.isStale(clock)) {
            LOG.warn("Event {} passed at {} and will not be published today", event.getType(), eventTime);
        } else {
            LOG.info("Scheduling event {} publication at {}", event.getType(), eventTime);
            var publisher = publisherProvider.get();
            publisher.setEvent(event);
            taskScheduler.schedule(publisher, event.getTimestamp());
        }
    }
}
