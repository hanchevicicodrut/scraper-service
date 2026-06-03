package com.chan.scraper_service.repositories;

import com.chan.scraper_service.entities.ProductPriceHistory;
import com.chan.scraper_service.entities.Product;
import com.chan.scraper_service.entities.ScrapeRun;
import com.chan.scraper_service.enums.PriceChangeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductPriceHistoryRepository
        extends JpaRepository<ProductPriceHistory, Long> {

    // ── BY PRODUCT ────────────────────────────────────────────────
    List<ProductPriceHistory> findByProductOrderByScrapedAtDesc(
            Product product);

    List<ProductPriceHistory> findByProduct_SkuOrderByScrapedAtDesc(
            String sku);

    // ── BY CHANGE TYPE ────────────────────────────────────────────
    List<ProductPriceHistory> findByChangeType(
            PriceChangeType changeType);

    List<ProductPriceHistory> findByChangeTypeAndScrapedAtAfter(
            PriceChangeType changeType, LocalDateTime after);

    // ── BY SCRAPE RUN ─────────────────────────────────────────────
    List<ProductPriceHistory> findByScrapeRun(ScrapeRun scrapeRun);

    // ── PRICE DROP QUERIES ────────────────────────────────────────
    @Query("""
        SELECT h FROM ProductPriceHistory h
        WHERE h.changeType = 'PRICE_DOWN'
        AND h.changePercentage <= :threshold
        AND h.scrapedAt >= :after
        ORDER BY h.changePercentage ASC
        """)
    List<ProductPriceHistory> findSignificantPriceDrops(
            @Param("threshold") java.math.BigDecimal threshold,
            @Param("after") LocalDateTime after);

    // ── LATEST SNAPSHOT PER PRODUCT ───────────────────────────────
    @Query("""
        SELECT h FROM ProductPriceHistory h
        WHERE h.product = :product
        AND h.scrapedAt = (
            SELECT MAX(h2.scrapedAt)
            FROM ProductPriceHistory h2
            WHERE h2.product = :product
        )
        """)
    java.util.Optional<ProductPriceHistory> findLatestByProduct(
            @Param("product") Product product);
}
