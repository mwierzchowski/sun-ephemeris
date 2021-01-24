package com.github.mwierzchowski.sun.core;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;

import static java.lang.Boolean.FALSE;
import static java.time.Duration.ofSeconds;
import static java.util.UUID.randomUUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SunEventPublisher {
    public static final String QUEUE_NAME = "sun-ephemeris.events";
    public static final String LOCK_NAME = "sun-ephemeris.publish-lock";

    private final SunEphemerisProvider provider;
    private final Clock clock;
    private final TaskScheduler scheduler;
    private final RedisTemplate<String, Object> redis;
    private final Environment env;

    @Scheduled(cron = "${sun-ephemeris.publish.cron}")
    @Retry(name = "SunEventPublisher", fallbackMethod = "planEventsFallback")
    public void planEvents() {
        LOG.info("Planning today events");
        var today = LocalDate.now(clock);
        provider.sunEphemerisFor(today).stream()
                .forEach(this::planEvent);
    }

    public void planEventsFallback(Throwable throwable) {
        LOG.warn("Planning events based on today ephemeris failed, trying yesterday", throwable);
        var yesterday = LocalDate.now(clock).minusDays(1);
        provider.sunEphemerisFor(yesterday).stream()
                .map(old -> new SunEvent(old.getType(), old.getTimestamp().plusSeconds(24L * 60 * 60)))
                .forEach(this::planEvent);
    }

    private void planEvent(SunEvent event) {
        if (event.isStale(clock)) {
            LOG.warn("Event {} will not be published today since it passed at {}",
                    event.getType(), event.getLocalDateTime(clock).toLocalTime());
            return;
        }
        scheduler.schedule(new Task(event), event.getTimestamp());
        LOG.info("Event {} scheduled for publishing today at {}", event.getType(), event.getLocalDateTime(clock).toLocalTime());
    }

    @RequiredArgsConstructor
    private class Task implements Runnable {
        private final SunEvent event;

        @Override
        public void run() {
            var lockDuration = env.getRequiredProperty("sun-ephemeris.publish.lock-duration", Long.class);
            var lockFlag = redis.opsForValue().setIfAbsent(LOCK_NAME, randomUUID(), ofSeconds(lockDuration));
            if (FALSE.equals(lockFlag)) {
                LOG.debug("Dropping event {} (not a leader)", event.getType());
                return;
            }
            LOG.info("Publishing event {}", event.getType());
            redis.convertAndSend(QUEUE_NAME, event);
        }
    }
}
