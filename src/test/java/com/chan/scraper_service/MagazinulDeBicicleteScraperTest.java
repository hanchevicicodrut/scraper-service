package com.chan.scraper_service;

import com.chan.scraper_service.dtos.ScrapedProductDto;
import com.chan.scraper_service.services.MagazinulDeBicicleteScraper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
public class MagazinulDeBicicleteScraperTest {

    @Autowired
    private MagazinulDeBicicleteScraper scraperService;

    @Test
    @DisplayName("should scrape one category and return enriched products")
    void shouldScrapeOneCategoryWithDetails() throws IOException {

        // Scrape only electrice category — fastest for testing
        List<ScrapedProductDto> products = scraperService
                .scrapeCategory("/biciclete/electrice");

        assertFalse(products.isEmpty(), "Should find at least one product");

        // Log first product for visual inspection
        ScrapedProductDto first = products.get(0);

        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("  Total products found:  {}", products.size());
        log.info("━━━ First product ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("  Name:         {}", first.getName());
        log.info("  SKU:          {}", first.getSku());
        log.info("  Brand:        {}", first.getBrand());
        log.info("  Category:     {}", first.getCategory());
        log.info("  Subcategory:  {}", first.getSubcategory());
        log.info("  Price:        {} {}", first.getPrice(), first.getCurrency());
        log.info("  In Stock:     {}", first.getInStock());
        log.info("  Sizes:        {}", first.getAvailableSizes());
        log.info("  Image:        {}", first.getImageUrl());
        log.info("  URL:          {}", first.getProductUrl());
        log.info("  Source:       {}", first.getSourceWebsite());
        log.info("  Description:  {}",
                first.getDescription() != null
                        ? first.getDescription().substring(0,
                        Math.min(100, first.getDescription().length())) + "..."
                        : "null");
        log.info("  Attributes ({}):",
                first.getAttributes() != null
                        ? first.getAttributes().size() : 0);
        if (first.getAttributes() != null) {
            first.getAttributes()
                    .forEach((k, v) -> log.info("    {} → {}", k, v));
        }
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // Assertions
        assertNotNull(first.getName(),       "Name should not be null");
        assertNotNull(first.getProductUrl(), "URL should not be null");
        assertNotNull(first.getPrice(),      "Price should not be null");
        assertTrue(first.getPrice().doubleValue() > 0, "Price should be > 0");
        assertEquals("magazinuldebiciclete.ro", first.getSourceWebsite());
        assertEquals("RON", first.getCurrency());
    }
}
