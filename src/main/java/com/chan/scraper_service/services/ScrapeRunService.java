package com.chan.scraper_service.services;

import com.chan.scraper_service.entities.ScrapeRun;
import com.chan.scraper_service.enums.ScrapeStatus;
import com.chan.scraper_service.repositories.ScrapeRunRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScrapeRunService {

    private final ScrapeRunRepository scrapeRunRepository;

    public ScrapeRun start(String sourceWebsite, String scrapeUrl) {
        ScrapeRun run = ScrapeRun.builder()
                .sourceWebsite(sourceWebsite)
                .scrapeUrl(scrapeUrl)
                .status(ScrapeStatus.RUNNING)
                .startedAt(Instant.now())
                .totalFound(0)
                .totalInserted(0)
                .totalUpdated(0)
                .totalUnchanged(0)
                .totalFailed(0)
                .build();

        ScrapeRun saved = scrapeRunRepository.save(run);
        log.info("🚀 Scrape run started: id={} website={}", saved.getId(), sourceWebsite);
        return saved;
    }

    public void finish(ScrapeRun run) {
        run.setStatus(ScrapeStatus.SUCCESS);
        run.setFinishedAt(Instant.now());
        scrapeRunRepository.save(run);
        log.info("✅ Scrape run finished: id={} inserted={} updated={} unchanged={} failed={}",
                run.getId(),
                run.getTotalInserted(),
                run.getTotalUpdated(),
                run.getTotalUnchanged(),
                run.getTotalFailed());
    }

    public void fail(ScrapeRun run, String errorMessage) {
        run.setStatus(ScrapeStatus.FAILED);
        run.setFinishedAt(Instant.now());
        run.setErrorMessage(errorMessage);
        scrapeRunRepository.save(run);
        log.error("❌ Scrape run failed: id={} error={}", run.getId(), errorMessage);
    }

    public void partial(ScrapeRun run) {
        run.setStatus(ScrapeStatus.PARTIAL);
        run.setFinishedAt(Instant.now());
        scrapeRunRepository.save(run);
        log.warn("⚠️ Scrape run partial: id={} failed={}", run.getId(), run.getTotalFailed());
    }
}
