package com.chan.scraper_service.controller;

import com.chan.scraper_service.services.ScraperOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/scraper")
public class ScraperController{
    private final ScraperOrchestrator orchestrator;

    @PostMapping("/run")
    public ResponseEntity<String> runScrape() {
        orchestrator.runFullScrape();
        return ResponseEntity.ok("Scrape started");
    }
}
