package com.chan.scraper_service.controller;

import com.chan.scraper_service.dtos.ScrapedProductDto;
import com.chan.scraper_service.services.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/product")
@Slf4j
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ScrapedProductDto>> getProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }
}
