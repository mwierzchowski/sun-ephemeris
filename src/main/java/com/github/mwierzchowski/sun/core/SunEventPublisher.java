package com.github.mwierzchowski.sun.core;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Slf4j
@Setter
@Component
@Scope(SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class SunEventPublisher implements Runnable {
    public static final String LOCK = "sun-ephemeris:publish";

    private final RedisTemplate<String, Object> redis;

    @Value("${publisher.channel}")
    private String channel;

    private SunEvent event;

    @Override
    @SchedulerLock(name = LOCK, lockAtLeastFor = "${publisher.lock-duration}")
    public void run() {
        LOG.info("Publishing event {}", event.getType());
        redis.convertAndSend(channel, event);
    }
}
