package com.chan.scraper_service;

import com.chan.scraper_service.services.SportguruScraperService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
public class SportguruScraperServiceTest {

    @Autowired
    private SportguruScraperService sportguruScraperService;


//    @Test
//    @DisplayName("should return results")
//    public void shouldReturnResults() {
//        var scrapedProductDtos = sportguruScraperService.scrapeListingPage();
//
//
//        scrapedProductDtos.forEach(p -> System.out.println("p.name: " + p.getName()));
//
//        assertFalse(scrapedProductDtos.isEmpty());
//
//    }
}
