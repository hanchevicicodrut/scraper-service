package com.chan.scraper_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@EnableJpaAuditing
@EnableScheduling
@SpringBootApplication
public class ScraperServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScraperServiceApplication.class, args);
	}

}
