package com.chan.scraper_service.scheduler;

import com.chan.scraper_service.services.ScraperOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
@Component
public class ScraperScheduler {

    private final ScraperOrchestrator scraperOrchestrator;

    @Scheduled(cron="${scraper.schedule.cron}")
    private void scheduledScraper(){
        log.info("Scraper scheduled start: {}", Instant.now().toString());
        scraperOrchestrator.runFullScrape();
    }

}
