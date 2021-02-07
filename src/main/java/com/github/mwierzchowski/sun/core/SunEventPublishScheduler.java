package com.github.mwierzchowski.sun.core;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
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
    private final Provider<SunEventPublishTask> taskProvider;
    private final TaskScheduler taskScheduler;
    private final ApplicationEventPublisher statusPublisher;
    private final Clock clock;

    @Scheduled(cron = "${sun-ephemeris.schedule-cron}")
    @Retry(name = "SunEventPublisher", fallbackMethod = "scheduleEventsFallback")
    public void scheduleEvents() {
        LOG.info("Scheduling today events publication");
        var today = LocalDate.now(clock);
        var ephemeris = ephemerisProvider.sunEphemerisFor(today);
        ephemeris.stream().forEach(this::scheduleEvent);
        statusPublisher.publishEvent(new SuccessEvent(ephemeris));
    }

    public void scheduleEventsFallback(Throwable throwable) {
        LOG.warn("Scheduling events publication based on today ephemeris failed, trying yesterday", throwable);
        statusPublisher.publishEvent(new FailureEvent(throwable));
        var yesterday = LocalDate.now(clock).minusDays(1);
        ephemerisProvider.sunEphemerisFor(yesterday).stream()
                .map(old -> new SunEvent(old.getType(), old.getTimestamp().plusSeconds(24L * 60 * 60)))
                .forEach(this::scheduleEvent);
    }

    private void scheduleEvent(SunEvent event) {
        var eventTime = event.getLocalTime(clock);
        if (event.isStale(clock)) {
            LOG.warn("Event {} passed at {} and will not be published today", event.getType(), eventTime);
        } else {
            LOG.info("Scheduling event {} publication at {}", event.getType(), eventTime);
            var task = taskProvider.get();
            task.setEvent(event);
            taskScheduler.schedule(task, event.getTimestamp());
        }
    }

    public static class SuccessEvent extends ApplicationEvent {
        public SuccessEvent(SunEphemeris ephemeris) {
            super(ephemeris);
        }
    }

    public static class FailureEvent extends ApplicationEvent {
        public FailureEvent(Throwable throwable) {
            super(throwable);
        }
    }
}
