package com.github.mwierzchowski.sun.core;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Provider;
import java.time.Clock;
import java.time.LocalDate;

@Slf4j
@Setter
@Getter
@Service
@RequiredArgsConstructor
public class SunEventPublishScheduler {
    private final SunEphemerisProvider ephemerisProvider;
    private final Provider<SunEventPublisher> publisherProvider;
    private final TaskScheduler taskScheduler;
    private final ApplicationEventPublisher statusPublisher;
    private final Clock clock;

    @Value("${scheduler.init-on-start}")
    private Boolean initOnStart;

    @Scheduled(cron = "${scheduler.cron}")
    @EventListener(classes = ApplicationReadyEvent.class, condition = "@sunEventPublishScheduler.initOnStart")
    @Retry(name = "SunEventPublishScheduler", fallbackMethod = "scheduleEventsFallback")
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
            var publisher = publisherProvider.get();
            publisher.setEvent(event);
            taskScheduler.schedule(publisher, event.getTimestamp());
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
