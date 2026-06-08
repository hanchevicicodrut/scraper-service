package com.chan.scraper_service.services;

import com.chan.scraper_service.dtos.ScrapedProductDto;
import com.chan.scraper_service.entities.ScrapeRun;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScraperOrchestrator {

    private final BikeXpertScraperService scraperService;
    private final ProductService          productService;
    private final ScrapeRunService        scrapeRunService;

    private static final String SOURCE  = "bikexpert.ro";
    private static final String SCRAPE_URL =
            "https://www.bikexpert.ro/biciclete/mountain-bike";

    /**
     * Scrapes a single product and persists it. Handy as a smoke test for the
     * scrape → save pipeline without running a full crawl.
     */
    public void runSingleProductTest() {
        ScrapeRun run = scrapeRunService.start(SOURCE, SCRAPE_URL);

        try {
            ScrapedProductDto product = scraperService.scrapeOneProductWithDetails();
            run.setTotalFound(1);

            productService.saveOrUpdate(product, run);

            scrapeRunService.finish(run);
        } catch (Exception e) {
            scrapeRunService.fail(run, e.getMessage());
            log.error("Single product scrape failed", e);
        }
    }

    public void runFullScrape() {
        ScrapeRun run = scrapeRunService.start(SOURCE, SCRAPE_URL);

        try {
            // 1. Scrape all pages
            List<ScrapedProductDto> products =
                    scraperService.scrapeAllPages();
            run.setTotalFound(products.size());

            // 2. Collect all scraped SKUs
            Set<String> scrapedSkus = products.stream()
                    .map(ScrapedProductDto::getSku)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            log.info("Total scraped SKUs: {}", scrapedSkus.size());

            // 3. Save each product
            for (ScrapedProductDto dto : products) {
                productService.saveOrUpdate(dto, run);
            }

            // 4. Deactivate missing products
            int deactivated = productService
                    .deactivateMissing(scrapedSkus, SOURCE);

            log.info("Deactivated: {} products", deactivated);

            // 5. Reactivate products that came back
            int reactivated = productService
                    .reactivateReturned(scrapedSkus, SOURCE);

            log.info("Reactivated: {} products", reactivated);

            // 6. Finish run
            if (run.getTotalFailed() > 0) {
                scrapeRunService.partial(run);
            } else {
                scrapeRunService.finish(run);
            }

        } catch (Exception e) {
            scrapeRunService.fail(run, e.getMessage());
            log.error("Scrape run failed", e);
        }
    }
}
