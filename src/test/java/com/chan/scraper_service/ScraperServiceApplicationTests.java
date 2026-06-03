package com.chan.scraper_service;

import com.chan.scraper_service.controller.ScraperController;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled
class ScraperServiceApplicationTests {

	@Autowired
	ScraperController scraperController;

	@Test
	void contextLoads() {
	}

}
