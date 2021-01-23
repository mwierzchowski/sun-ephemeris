package com.github.mwierzchowski.sun;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;

@EnableAsync
@EnableCaching
@EnableScheduling
@SpringBootApplication
public class SunApplication {
	@Bean
	Clock systemClock() {
		return Clock.systemDefaultZone();
	}

	public static void main(String[] args) {
		SpringApplication.run(SunApplication.class, args);
	}
}
