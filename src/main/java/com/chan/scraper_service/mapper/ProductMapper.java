package com.chan.scraper_service.mapper;

import com.chan.scraper_service.dtos.ScrapedProductDto;
import com.chan.scraper_service.entities.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",    // makes it a Spring @Component
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProductMapper {

    // ── ScrapedProductDto → new Product ──────────────────────────
    @Mapping(target = "id",             ignore = true)
    @Mapping(target = "firstScrapedAt", ignore = true)
    @Mapping(target = "lastScrapedAt",  ignore = true)
    @Mapping(target = "embedding",      ignore = true)
    @Mapping(target = "priceHistory",   ignore = true)
    @Mapping(source = "price",          target = "currentPrice")
    @Mapping(target = "company", ignore = true)
    Product toEntity(ScrapedProductDto dto);

    // ── Update EXISTING product from new scrape ───────────────────
    @Mapping(target = "id",             ignore = true)
    @Mapping(target = "firstScrapedAt", ignore = true)
    @Mapping(target = "lastScrapedAt",  ignore = true)
    @Mapping(target = "embedding",      ignore = true)
    @Mapping(target = "priceHistory",   ignore = true)
    @Mapping(source = "price",          target = "currentPrice")
    @Mapping(target = "company", ignore = true)
    void updateEntity(ScrapedProductDto dto, @MappingTarget Product product);

    // ── Product → ScrapedProductDto ───────────────────────────────
    @Mapping(source = "currentPrice", target = "price")
    @Mapping(target = "productId",    ignore = true)
    @Mapping(target = "discount",     ignore = true)
    ScrapedProductDto toDto(Product product);
}
