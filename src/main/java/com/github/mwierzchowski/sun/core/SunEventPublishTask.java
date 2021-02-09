package com.github.mwierzchowski.sun.core;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Slf4j
@Component
@Scope(SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class SunEventPublishTask implements Runnable {
    public static final String QUEUE_NAME = "sun-ephemeris:events";
    public static final String LOCK_NAME = "sun-ephemeris:publish";

    private final RedisTemplate<String, Object> redis;

    @Setter
    private SunEvent event;

    @Override
    @SchedulerLock(name = LOCK_NAME, lockAtLeastFor = "${sun-ephemeris.publish-lock-duration}")
    public void run() {
        LOG.info("Publishing event {}", event.getType());
        redis.convertAndSend(QUEUE_NAME, event);
    }
}
