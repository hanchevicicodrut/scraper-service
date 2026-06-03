package com.chan.scraper_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScrapedProductDto {

    // FROM LISTING PAGE
    private String productId;           // data-id
    private String sku;                 // real SKU from detail page
    private String name;
    private String productUrl;
    private String imageUrl;
    private BigDecimal price;
    private BigDecimal originalPrice;   // ← ADD
    private String currency;
    private String category;
    private String brand;
    private Boolean inStock;
    private String sourceWebsite;
    private String discount;

    // FROM DETAIL PAGE                 ← ALL MISSING
    private String subcategory;         // "Trail/All mountain"
    private String description;         // full description text
    private List<String> availableSizes;   // ["S", "M", "L", "XL"]
    private List<String> availableColors;  // ["Rosu", "Negru"]
    private Map<String, String> attributes; // all specs from param table
}
