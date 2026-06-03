package com.chan.scraper_service;

import com.chan.scraper_service.dtos.ScrapedProductDto;
import com.chan.scraper_service.services.BikeXpertScraperService;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.constraintvalidators.bv.AssertFalseValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
public class BikeXpertScraperServiceTest {

    @Autowired
    private BikeXpertScraperService bikeXpertScraperService;

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
}
