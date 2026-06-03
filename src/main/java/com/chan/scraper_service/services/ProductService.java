package com.chan.scraper_service.services;

import com.chan.scraper_service.dtos.ScrapedProductDto;
import com.chan.scraper_service.entities.Product;
import com.chan.scraper_service.entities.ProductPriceHistory;
import com.chan.scraper_service.entities.ScrapeRun;
import com.chan.scraper_service.enums.PriceChangeType;
import com.chan.scraper_service.mapper.ProductMapper;
import com.chan.scraper_service.repositories.ProductPriceHistoryRepository;
import com.chan.scraper_service.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductPriceHistoryRepository priceHistoryRepository;
    private final ProductMapper productMapper;

    // ─────────────────────────────────────────────────────────────
    // MAIN METHOD — called for each scraped product
    // ─────────────────────────────────────────────────────────────

    @Transactional
    public void saveOrUpdate(ScrapedProductDto dto, ScrapeRun scrapeRun) {

        if (dto.getSku() == null || dto.getSku().isBlank()) {
            log.warn("Skipping product with null SKU: {}", dto.getName());
            scrapeRun.setTotalFailed(scrapeRun.getTotalFailed() + 1);
            return;
        }

        Optional<Product> existing = productRepository.findBySku(dto.getSku());

        if (existing.isEmpty()) {
            insertNewProduct(dto, scrapeRun);
        } else {
            updateExistingProduct(dto, existing.get(), scrapeRun);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // INSERT — new product never seen before
    // ─────────────────────────────────────────────────────────────

    private void insertNewProduct(ScrapedProductDto dto, ScrapeRun scrapeRun) {
        log.info("  ➕ Inserting new product: [{}] {}", dto.getSku(), dto.getName());

        // Map DTO → Entity
        Product product = productMapper.toEntity(dto);
        Product saved = productRepository.save(product);

        // Record first price history
        ProductPriceHistory history = ProductPriceHistory.builder()
                .product(saved)
                .scrapeRun(scrapeRun)
                .price(dto.getPrice())
                .originalPrice(dto.getOriginalPrice())
                .currency(dto.getCurrency())
                .inStock(dto.getInStock())
                .changeType(PriceChangeType.FIRST_SEEN)
                .previousPrice(null)
                .changePercentage(null)
                .build();

        priceHistoryRepository.save(history);

        // Update scrape run counter
        scrapeRun.setTotalInserted(scrapeRun.getTotalInserted() + 1);
        log.info("  ✅ Inserted: [{}] {}", saved.getSku(), saved.getName());
    }

    // ─────────────────────────────────────────────────────────────
    // UPDATE — product already exists
    // ─────────────────────────────────────────────────────────────

    private void updateExistingProduct(ScrapedProductDto dto,
                                       Product existing,
                                       ScrapeRun scrapeRun) {

        BigDecimal oldPrice  = existing.getCurrentPrice();
        Boolean    oldStock  = existing.getInStock();
        BigDecimal newPrice  = dto.getPrice();
        Boolean    newStock  = dto.getInStock();

        // Detect what changed
        PriceChangeType changeType = detectChangeType(
                oldPrice, oldStock, newPrice, newStock
        );

        if (changeType == PriceChangeType.NO_CHANGE) {
            // Just update lastScrapedAt — no history record needed
            existing.setLastScrapedAt(LocalDateTime.now());
            productRepository.save(existing);
            scrapeRun.setTotalUnchanged(scrapeRun.getTotalUnchanged() + 1);
            log.info("  ↔️  No change: [{}] {}", existing.getSku(), existing.getName());
            return;
        }

        log.info("  🔄 Updating [{}]: {} → {} ({})",
                existing.getSku(), oldPrice, newPrice, changeType);

        // Update entity with new data
        productMapper.updateEntity(dto, existing);
        productRepository.save(existing);

        // Calculate change percentage
        BigDecimal changePercentage = calculateChangePercentage(oldPrice, newPrice);

        // Record history
        ProductPriceHistory history = ProductPriceHistory.builder()
                .product(existing)
                .scrapeRun(scrapeRun)
                .price(newPrice)
                .originalPrice(dto.getOriginalPrice())
                .currency(dto.getCurrency())
                .inStock(newStock)
                .changeType(changeType)
                .previousPrice(oldPrice)
                .changePercentage(changePercentage)
                .build();

        priceHistoryRepository.save(history);
        scrapeRun.setTotalUpdated(scrapeRun.getTotalUpdated() + 1);
    }

    // ─────────────────────────────────────────────────────────────
    // DETECT CHANGE TYPE
    // ─────────────────────────────────────────────────────────────

    private PriceChangeType detectChangeType(BigDecimal oldPrice,
                                             Boolean    oldStock,
                                             BigDecimal newPrice,
                                             Boolean    newStock) {

        // Stock changes take priority
        if (Boolean.FALSE.equals(oldStock) && Boolean.TRUE.equals(newStock)) {
            return PriceChangeType.BACK_IN_STOCK;
        }
        if (Boolean.TRUE.equals(oldStock) && Boolean.FALSE.equals(newStock)) {
            return PriceChangeType.OUT_OF_STOCK;
        }

        // Price changes
        if (oldPrice != null && newPrice != null) {
            int comparison = oldPrice.compareTo(newPrice);
            if (comparison > 0) return PriceChangeType.PRICE_DOWN;
            if (comparison < 0) return PriceChangeType.PRICE_UP;
        }

        return PriceChangeType.NO_CHANGE;
    }

    // ─────────────────────────────────────────────────────────────
    // CALCULATE CHANGE PERCENTAGE
    // ─────────────────────────────────────────────────────────────

    private BigDecimal calculateChangePercentage(BigDecimal oldPrice,
                                                 BigDecimal newPrice) {
        if (oldPrice == null || newPrice == null
                || oldPrice.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        // ((newPrice - oldPrice) / oldPrice) * 100
        return newPrice
                .subtract(oldPrice)
                .divide(oldPrice, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional
    public int deactivateMissing(Set<String> currentSkus, String sourceWebsite) {

        List<Product> activeProducts = productRepository
                .findBySourceWebsiteAndActive(sourceWebsite, true);

        List<Product> toDeactivate = activeProducts.stream()
                .filter(p -> p.getSku() != null
                        && !currentSkus.contains(p.getSku()))
                .collect(Collectors.toList());

        toDeactivate.forEach(p -> {
            p.setActive(false);
            p.setDeactivatedAt(LocalDateTime.now());
            log.info("  🔴 Deactivated: [{}] {}", p.getSku(), p.getName());
        });

        productRepository.saveAll(toDeactivate);
        return toDeactivate.size();
    }

    @Transactional
    public int reactivateReturned(Set<String> currentSkus, String sourceWebsite) {

        // Find inactive products that ARE in current scrape
        List<Product> toReactivate = productRepository
                .findBySourceWebsiteAndActive(sourceWebsite, false)
                .stream()
                .filter(p -> p.getSku() != null
                        && currentSkus.contains(p.getSku()))
                .collect(Collectors.toList());

        toReactivate.forEach(p -> {
            p.setActive(true);
            p.setDeactivatedAt(null);
            log.info("  🟢 Reactivated: [{}] {}", p.getSku(), p.getName());
        });

        productRepository.saveAll(toReactivate);
        return toReactivate.size();
    }
}
