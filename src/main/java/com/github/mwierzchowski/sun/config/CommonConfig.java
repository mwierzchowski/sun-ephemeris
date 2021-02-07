package com.github.mwierzchowski.sun.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;
import java.time.format.DateTimeFormatter;

import static java.time.format.DateTimeFormatter.ofPattern;

@EnableAsync
@EnableCaching
@EnableScheduling
@Configuration
public class CommonConfig {
    @Bean
    Clock systemClock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    DateTimeFormatter timestampFormatter(Clock clock) {
        return ofPattern("yyyy-MM-dd HH:mm:ss").withZone(clock.getZone());
    }
}
