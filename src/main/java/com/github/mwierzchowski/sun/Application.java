package com.github.mwierzchowski.sun;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
	@Value("${init-on-startup:true}")
	@Getter Boolean initOnStartup;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
