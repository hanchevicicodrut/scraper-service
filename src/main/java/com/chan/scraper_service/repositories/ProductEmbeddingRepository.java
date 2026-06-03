package com.chan.scraper_service.repositories;

import com.chan.scraper_service.entities.ProductEmbedding;
import com.chan.scraper_service.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface ProductEmbeddingRepository
        extends JpaRepository<ProductEmbedding, Long> {

    Optional<ProductEmbedding> findByProduct(Product product);

    Optional<ProductEmbedding> findByProduct_Sku(String sku);

    boolean existsByProduct(Product product);

    List<ProductEmbedding> findByModelName(String modelName);

    List<ProductEmbedding> findByModelProvider(String modelProvider);
}
