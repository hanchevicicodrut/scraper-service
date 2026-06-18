package com.chan.scraper_service.repositories;

import com.chan.scraper_service.entities.Company;
import com.chan.scraper_service.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // ── IDENTITY ──────────────────────────────────────────────────
    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    Optional<Product> findByProductUrl(String productUrl);

    // ── FILTERS ───────────────────────────────────────────────────
    List<Product> findByCategory(String category);

    List<Product> findByBrand(String brand);

    List<Product> findByInStock(Boolean inStock);

    List<Product> findByBrandAndInStock(String brand, Boolean inStock);

    List<Product> findByCategoryAndInStock(String category, Boolean inStock);

    List<Product> findByCurrentPriceLessThanEqual(BigDecimal price);

    List<Product> findByCurrentPriceBetween(BigDecimal min, BigDecimal max);

    List<Product> findBySourceWebsite(String sourceWebsite);

    // ── SEARCH ────────────────────────────────────────────────────
    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByBrandAndCategoryAndInStock(
            String brand, String category, Boolean inStock);

    // ── JSONB QUERIES ─────────────────────────────────────────────
    @Query(value = """
        SELECT * FROM products
        WHERE attributes->>'Diametru roata [inch]' = :wheelSize
        AND in_stock = true
        """, nativeQuery = true)
    List<Product> findByWheelSize(@Param("wheelSize") String wheelSize);

    @Query(value = """
        SELECT * FROM products
        WHERE :size = ANY(
            SELECT jsonb_array_elements_text(available_sizes)
        )
        AND in_stock = true
        """, nativeQuery = true)
    List<Product> findByAvailableSize(@Param("size") String size);

    // ── STATS ─────────────────────────────────────────────────────
    long countByInStock(Boolean inStock);

    long countBySourceWebsite(String sourceWebsite);

    long countByBrand(String brand);

    List<Product> findBySourceWebsiteAndActive(String sourceWebsite, Boolean active);

    List<Product> findByActive(Boolean active);

    List<Product> findByCompany(Company company);

    List<Product> findByCompany_Id(Long companyId);
}
