package com.chan.scraper_service;

import com.chan.scraper_service.dtos.ScrapedProductDto;
import com.chan.scraper_service.entities.Product;
import com.chan.scraper_service.repositories.ProductRepository;
import com.chan.scraper_service.services.BikeXpertScraperService;
import com.chan.scraper_service.services.ScraperOrchestrator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.Optional;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
public class BikeXpertScraperServiceTest {

    @Autowired
    private BikeXpertScraperService bikeXpertScraperService;

    @Autowired
    private ScraperOrchestrator scraperOrchestrator;

    @Autowired
    private ProductRepository productRepository;

//    @Test
//    @DisplayName("should return 200")
//    void shouldReturn200() {
//        var scrapedProductDtos = bikeXpertScraperService.scrapeListingPage();
//        scrapedProductDtos.forEach(p -> System.out.println("name: " + p.getName()));
//        assertFalse(scrapedProductDtos.isEmpty());
//    }

    @Test
    @DisplayName("should scrape one product with full details")
    void shouldScrapeOneProductWithDetails() {

        ScrapedProductDto product = bikeXpertScraperService
                .scrapeOneProductWithDetails();

        assertNotNull(product, "Product should not be null");

        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("  Name:         {}", product.getName());
        log.info("  SKU:          {}", product.getSku());
        log.info("  Brand:        {}", product.getBrand());
        log.info("  Category:     {}", product.getCategory());
        log.info("  Subcategory:  {}", product.getSubcategory());
        log.info("  Price:        {} {}", product.getPrice(), product.getCurrency());
        log.info("  Orig. Price:  {}", product.getOriginalPrice());
        log.info("  Discount:     {}", product.getDiscount());
        log.info("  In Stock:     {}", product.getInStock());
        log.info("  Sizes:        {}", product.getAvailableSizes());
        log.info("  Colors:       {}", product.getAvailableColors());
        log.info("  Image:        {}", product.getImageUrl());
        log.info("  URL:          {}", product.getProductUrl());
        log.info("  Description:  {}",
                product.getDescription() != null
                        ? product.getDescription().substring(0,
                        Math.min(100, product.getDescription().length())) + "..."
                        : "null");
        log.info("  Attributes ({}):",
                product.getAttributes() != null ? product.getAttributes().size() : 0);
        if (product.getAttributes() != null) {
            product.getAttributes()
                    .forEach((k, v) -> log.info("    {} → {}", k, v));
        }
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // Assertions
        assertNotNull(product.getName());
        assertNotNull(product.getPrice());
        assertNotNull(product.getProductUrl());
        assertTrue(product.getInStock(), "Product should be in stock");
        assertNotNull(product.getAvailableSizes());
        assertFalse(product.getAvailableSizes().isEmpty(),
                "Should have at least one available size");
        assertNotNull(product.getAttributes());
        assertFalse(product.getAttributes().isEmpty(),
                "Should have at least one attribute");
    }

    @Test
    @DisplayName("should scrape and persist a single product")
    void shouldScrapeAndSaveOneProduct() {
        System.out.println("JVM Timezone: " + TimeZone.getDefault().getID());
        System.out.println("Current Instant: " + Instant.now());
        ScrapedProductDto scraped = bikeXpertScraperService.scrapeOneProductWithDetails();
        assertNotNull(scraped, "Scraped product should not be null");
        assertNotNull(scraped.getSku(), "Scraped product should have a SKU");

        scraperOrchestrator.runSingleProductTest();

        Optional<Product> saved = productRepository.findBySku(scraped.getSku());
        assertTrue(saved.isPresent(), "Product should have been persisted to the database");

        log.info("Persisted product: [{}] {} — {} {}",
                saved.get().getSku(), saved.get().getName(),
                saved.get().getCurrentPrice(), saved.get().getCurrency());
    }
}
