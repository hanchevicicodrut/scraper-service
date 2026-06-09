package com.chan.scraper_service.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- IDENTITY ---
    @Column(unique = true, nullable = false)
    private String sku;

    @Column(nullable = false)
    private String sourceWebsite;      // "sportguru.ro"

    @Column(nullable = false)
    private String productUrl;

    // --- CORE SEARCHABLE (will be embedded) ---
    @Column(nullable = false)
    private String name;

    private String brand;
    private String category;
    private String subcategory;

    @Column(columnDefinition = "text")
    private String description;

    // --- HARD FILTERS (structured, queryable) ---
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal currentPrice;

    private BigDecimal originalPrice;
    private String currency;           // "RON"
    private Boolean inStock;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> availableSizes;  // ["M", "L", "XL"]

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> availableColors;

    private String imageUrl;

    // --- FLEXIBLE SPECS (varies by product type) ---
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> attributes;
    // bikes:  {"wheel_size":"29", "speeds":"1x12", ...}
    // shoes:  {"material":"leather", "waterproof":"true"}
    // helmet: {"certification":"EN1078", "visor":"true"}

    @CreatedDate
    private Instant firstScrapedAt;

    @LastModifiedDate
    private Instant lastScrapedAt;

    // --- RELATIONSHIP ---
    @OneToOne(mappedBy = "product",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private ProductEmbedding embedding;

    @OneToMany(mappedBy = "product",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private List<ProductPriceHistory> priceHistory;

    @Builder.Default
    private Boolean active = true;

    private Instant deactivatedAt;
}
