package com.github.mwierzchowski.sun;

import com.github.mwierzchowski.sun.core.SunEventPublishScheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;

@Slf4j
@SpringBootApplication
public class SunApplication {
	@Bean
	@ConditionalOnProperty(name = "sun-ephemeris.init-on-startup")
	ApplicationListener<ApplicationReadyEvent> initializer() {
		return event -> {
			LOG.info("Initializing on startup");
			event.getApplicationContext().getBean(SunEventPublishScheduler.class).scheduleEvents();
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(SunApplication.class, args);
	}
}
