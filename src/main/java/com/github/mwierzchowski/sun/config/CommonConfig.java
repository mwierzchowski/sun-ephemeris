package com.github.mwierzchowski.sun.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;

@EnableAsync
@EnableCaching
@EnableScheduling
@Configuration
public class CommonConfig {
    @Bean
    Clock systemClock() {
        return Clock.systemDefaultZone();
    }
}
