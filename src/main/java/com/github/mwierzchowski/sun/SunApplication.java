package com.github.mwierzchowski.sun;

import com.github.mwierzchowski.sun.core.SunEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;

@Slf4j
@EnableAsync
@EnableCaching
@EnableScheduling
@SpringBootApplication
public class SunApplication {
	@Bean
	Clock systemClock() {
		return Clock.systemDefaultZone();
	}

	@Bean
	@ConditionalOnProperty(name = "sun-ephemeris.init-on-startup")
	ApplicationListener<ApplicationReadyEvent> initializer() {
		return event -> {
			LOG.info("Initializing on startup");
			event.getApplicationContext().getBean(SunEventPublisher.class).planEvents();
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(SunApplication.class, args);
	}
}
