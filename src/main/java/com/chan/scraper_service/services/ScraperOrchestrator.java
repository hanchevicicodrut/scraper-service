package com.chan.scraper_service.services;

import com.chan.scraper_service.dtos.ScrapedProductDto;
import com.chan.scraper_service.entities.ScrapeRun;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScraperOrchestrator {

    private final BikeXpertScraperService scraperService;

    private final MagazinulDeBicicleteScraper magazinulScraper;
    private final ProductService productService;
    private final ScrapeRunService scrapeRunService;

    private static final String SOURCE = "bikexpert.ro";
    private static final List<String> SCRAPE_URLS = List.of(
            "https://www.bikexpert.ro/biciclete/mountain-bike",
            "https://www.bikexpert.ro/biciclete/sosea-ciclocross",
            "https://www.bikexpert.ro/biciclete/trekking-oras",
            "https://www.bikexpert.ro/biciclete/copii",
            "https://www.bikexpert.ro/biciclete/electrice",
            "https://www.bikexpert.ro/biciclete/pliabile",
            "https://www.bikexpert.ro/biciclete/bmx-street-dirt"
    );
    /*private static final List<String> SCRAPE_URLS = List.of(
            "https://www.bikexpert.ro/biciclete/sosea-ciclocross"
    );*/

    private static final String MAGAZINUL_SOURCE  = "magazinuldebiciclete.ro";
    private static final String MAGAZINUL_COMPANY = "MagazinulDeBiciclete";
    private static final List<String> MAGAZINUL_URLS = List.of(
            "/biciclete/electrice",
            "/biciclete/biciclete-sosea",
            "/biciclete/biciclete-triatlon",
            "/biciclete/biciclete-gravel",
            "/biciclete/biciclete-cross-country",
            "/biciclete/biciclete-trail-am",
            "/biciclete/biciclete-enduro",
            "/biciclete/biciclete-trekking",
            "/biciclete/biciclete-oras-fitness",
            "/biciclete/copii"
    );

//    private static final List<String> MAGAZINUL_URLS = List.of(
//            "/biciclete/electrice"
//    );


    // ─────────────────────────────────────────────────────────────
    // RUN ALL
    // ─────────────────────────────────────────────────────────────

    public void runAllScrapers() {
        log.info("🚀 Starting ALL scrapers");
        runFullScrape();
        runMagazinul();
        log.info("✅ ALL scrapers finished");
    }

    /**
     * Scrapes a single product and persists it. Handy as a smoke test for the
     * scrape → save pipeline without running a full crawl.
     */
    public void runSingleProductTest() {
        String url = SCRAPE_URLS.get(0);
        ScrapeRun run = scrapeRunService.start(SOURCE, url);

        try {
            ScrapedProductDto product = scraperService.scrapeOneProductWithDetails(url);
            run.setTotalFound(1);

            productService.saveOrUpdate(product, run, "BikeXpert");

            scrapeRunService.finish(run);
        } catch (Exception e) {
            scrapeRunService.fail(run, e.getMessage());
            log.error("Single product scrape failed", e);
        }
    }

    public void runFullScrape() {
        ScrapeRun run = scrapeRunService.start(SOURCE, String.join(",", SCRAPE_URLS));
        var now = Instant.now();
        log.debug("runFullScrape() - start - {}", now);
        try {
            // 1. Scrape all pages for every category URL
            List<ScrapedProductDto> products = new ArrayList<>();
            for (String url : SCRAPE_URLS) {
                log.info("━━━ Starting category: {}", url);
                products.addAll(scraperService.scrapeAllPages(url));
            }

            if (products.isEmpty()) {
                scrapeRunService.fail(run, "No products scraped after all retries");
                return;
            }
            run.setTotalFound(products.size());

            // 2. Collect all scraped SKUs
            Set<String> scrapedSkus = products.stream()
                    .map(ScrapedProductDto::getSku)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            log.info("Total scraped SKUs: {}", scrapedSkus.size());

            // 3. Save each product
            for (ScrapedProductDto dto : products) {
                productService.saveOrUpdate(dto, run, "BikeXpert");
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
            log.error("runFullScrape() - error end: {}", Duration.between(now, Instant.now()));
        }
        log.debug("runFullScrape() - end: {}", Duration.between(now, Instant.now()));
    }

    // ─────────────────────────────────────────────────────────────
    // MAGAZINUL DE BICICLETE — specialized flow
    // ─────────────────────────────────────────────────────────────

    public void runMagazinul() {
        ScrapeRun run = scrapeRunService.start(
                MAGAZINUL_SOURCE, String.join(",", MAGAZINUL_URLS));
        Instant now = Instant.now();
        log.debug("runMagazinul() - start - {}", now);

        try {
            // Scrape all MagazinulDeBiciclete categories
            List<ScrapedProductDto> products = new ArrayList<>();
            for (String url : MAGAZINUL_URLS) {
                log.info("━━━ Starting MagazinulDeBiciclete category: {}", url);
                products.addAll(magazinulScraper.scrapeCategory(url));
            }

            // Generic DB processing
            processProducts(products, run, MAGAZINUL_COMPANY, MAGAZINUL_SOURCE);

        } catch (Exception e) {
            scrapeRunService.fail(run, e.getMessage());
            log.error("MagazinulDeBiciclete scrape failed", e);
            log.error("runMagazinul() - error end: {}",
                    Duration.between(now, Instant.now()));
        }
        log.debug("runMagazinul() - end: {}",
                Duration.between(now, Instant.now()));
    }

    // ─────────────────────────────────────────────────────────────
    // GENERIC DB PROCESSING — same for all shops
    // ─────────────────────────────────────────────────────────────

    private void processProducts(List<ScrapedProductDto> products,
                                 ScrapeRun run,
                                 String companyName,
                                 String sourceWebsite) {
        if (products.isEmpty()) {
            scrapeRunService.fail(run, "No products scraped after all retries");
            return;
        }

        run.setTotalFound(products.size());
        log.info("Total products to process: {}", products.size());
        products.forEach(p ->
                log.debug("Product: sku={} | url={}", p.getSku(), p.getProductUrl())
        );
        // Collect scraped SKUs
        Set<String> scrapedSkus = products.stream()
                .map(ScrapedProductDto::getSku)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        log.info("Total valid SKUs: {}", scrapedSkus.size());

        // Save or update each product
        for (ScrapedProductDto dto : products) {
            productService.saveOrUpdate(dto, run, companyName);
        }

        // Deactivate products no longer on website
        int deactivated = productService
                .deactivateMissing(scrapedSkus, sourceWebsite);
        log.info("Deactivated: {} products", deactivated);

        // Reactivate products that came back
        int reactivated = productService
                .reactivateReturned(scrapedSkus, sourceWebsite);
        log.info("Reactivated: {} products", reactivated);

        // Finish run
        if (run.getTotalFailed() > 0) {
            scrapeRunService.partial(run);
        } else {
            scrapeRunService.finish(run);
        }
    }
}
