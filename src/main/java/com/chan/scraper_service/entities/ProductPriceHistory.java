package com.chan.scraper_service.entities;

import com.chan.scraper_service.enums.PriceChangeType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_price_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- RELATIONSHIP ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scrape_run_id", nullable = false)
    private ScrapeRun scrapeRun;

    // --- SNAPSHOT (what we saw at this moment) ---
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    private BigDecimal originalPrice;  // before discount

    private String currency;           // "RON"

    private Boolean inStock;

    // --- CHANGE TRACKING ---
    @Enumerated(EnumType.STRING)
    private PriceChangeType changeType;
    // FIRST_SEEN, PRICE_UP, PRICE_DOWN, BACK_IN_STOCK, OUT_OF_STOCK, NO_CHANGE

    private BigDecimal previousPrice;  // null if FIRST_SEEN

    // calculated: (price - previousPrice) / previousPrice * 100
    @Column(precision = 5, scale = 2)
    private BigDecimal changePercentage;

    // --- AUDIT ---
    @CreationTimestamp
    private Instant scrapedAt;
}
