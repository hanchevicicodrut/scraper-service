package com.chan.scraper_service.services;

import com.chan.scraper_service.dtos.ScrapedProductDto;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
public class BikeXpertScraperService {

    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/137.0.0.0 Safari/537.36";

    private static final String BASE_URL    = "https://www.bikexpert.ro";
    private static final String LISTING_URL = BASE_URL + "/biciclete/mountain-bike";
    private static final String PAGE_URL    = LISTING_URL + "/pag-";

    private final Random random = new Random();

    // ─────────────────────────────────────────────────────────────
    // PUBLIC API
    // ─────────────────────────────────────────────────────────────

    public List<ScrapedProductDto> scrapeAllPages() {
        List<ScrapedProductDto> allProducts = new ArrayList<>();

        int totalPages = findTotalPages();
        log.info("Total pages found: {}", totalPages);

        for (int page = 1; page <= totalPages; page++) {
            String url = page == 1 ? LISTING_URL : PAGE_URL + page;
            log.info("━━━ Scraping page {}/{}: {}", page, totalPages, url);

            List<ScrapedProductDto> pageProducts = scrapeListingPage(url);

            for (ScrapedProductDto dto : pageProducts) {
                enrichWithDetailPage(dto);
                randomDelay();

                // Only keep products with available sizes
                if (Boolean.TRUE.equals(dto.getInStock())) {
                    allProducts.add(dto);
                }
            }

            randomDelay();
        }

        log.info("━━━ Scrape complete. Available products: {}", allProducts.size());
        return allProducts;
    }

    public List<ScrapedProductDto> scrapeListingPage(String url) {
        List<ScrapedProductDto> results = new ArrayList<>();
        try {
            Document doc = connect(url);
            log.info("Page fetched: {}", doc.title());

            Elements products = doc.select("div.product");
            log.info("Products found on page: {}", products.size());

            for (Element product : products) {
                try {
                    results.add(parseProductCard(product));
                } catch (Exception e) {
                    log.warn("Failed to parse product card: {}", e.getMessage());
                }
            }
        } catch (IOException e) {
            log.error("Failed to scrape listing page: {}", url, e);
        }
        return results;
    }

    public ScrapedProductDto scrapeOneProductWithDetails() {
        List<ScrapedProductDto> products = scrapeListingPage(LISTING_URL);

        for (ScrapedProductDto dto : products) {
            enrichWithDetailPage(dto);
            randomDelay();

            // Return first available product
            if (Boolean.TRUE.equals(dto.getInStock())) {
                return dto;
            }
        }

        log.warn("No available products found on first page");
        return null;
    }

    public void enrichWithDetailPage(ScrapedProductDto dto) {
        if (dto.getProductUrl() == null) return;

        try {
            Document doc = connect(dto.getProductUrl());

            // ── AVAILABLE SIZES ───────────────────────────────────
            // Available = has pbc_NUMBER class AND no 'disable' class
            List<String> availableSizes = doc.select("a.type-buttons")
                    .stream()
                    .filter(el -> {
                        boolean hasPbcNumber = el.classNames().stream()
                                .anyMatch(c -> c.matches("pbc_\\d+"));
                        boolean isDisabled = el.hasClass("disable");
                        return hasPbcNumber && !isDisabled;
                    })
                    .map(el -> el.text().trim())
                    .filter(t -> !t.isEmpty())
                    .distinct()
                    .collect(Collectors.toList());

            // ── SKIP RULE ─────────────────────────────────────────
            if (availableSizes.isEmpty()) {
                dto.setInStock(false);
                log.info("  ⏭️  No available sizes — skipping: {}", dto.getName());
                return;
            }

            dto.setAvailableSizes(availableSizes);
            dto.setInStock(true);

            // ── SKU ───────────────────────────────────────────────
            Element skuEl = doc.selectFirst(".product-code");
            if (skuEl != null) {
                dto.setSku(skuEl.text()
                        .replace("Cod produs:", "")
                        .trim());
            }

            // ── BRAND ─────────────────────────────────────────────
            Element brandEl = doc.selectFirst(".product-brand");
            if (brandEl != null) {
                dto.setBrand(brandEl.text()
                        .replace("Marca:", "")
                        .trim());
            }

            // ── ORIGINAL PRICE ────────────────────────────────────
            Element oldPriceEl = doc.selectFirst(".product-second-price-section");
            if (oldPriceEl != null) {
                String rawOldPrice = oldPriceEl.text()
                        .replaceAll("[^0-9.]", "")
                        .replaceAll("\\.(?=.*\\.)", "");
                if (!rawOldPrice.isEmpty()) {
                    dto.setOriginalPrice(new BigDecimal(rawOldPrice));
                }
            }

            // ── DESCRIPTION ───────────────────────────────────────
            Element descEl = doc.selectFirst(".product-description-content");
            if (descEl != null) {
                dto.setDescription(descEl.text().trim());
            }

            // ── ATTRIBUTES FROM PARAM TABLE ───────────────────────
            Map<String, String> attributes = new LinkedHashMap<>();
            Elements rows = doc.select(".product-param-table tr");

            for (Element row : rows) {
                Elements cols = row.select("td");
                if (cols.size() >= 2) {
                    String key   = cols.get(0).text().trim();
                    String value = cols.get(1).text().trim();

                    if (!key.isEmpty() && !value.isEmpty()) {
                        attributes.put(key, value);

                        // Map known keys to dedicated fields
                        switch (key.toLowerCase()) {
                            case "model bicicleta" ->
                                    dto.setSubcategory(value);
                            case "culoare" ->
                                    dto.setAvailableColors(
                                            Arrays.asList(value.split("[,/]")));
                        }
                    }
                }
            }

            dto.setAttributes(attributes);

            log.info("  ✅ [{}] brand={} sizes={} attrs={}",
                    dto.getName(),
                    dto.getBrand(),
                    availableSizes,
                    attributes.size());

        } catch (Exception e) {
            log.warn("  ⚠️  Failed to enrich: {} → {}",
                    dto.getProductUrl(), e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────

    private int findTotalPages() {
        try {
            Document doc = connect(LISTING_URL);
            return doc.select("a[href*=pag-]")
                    .stream()
                    .map(el -> el.attr("href"))
                    .map(href -> href.replaceAll(".*pag-(\\d+).*", "$1"))
                    .filter(s -> s.matches("\\d+"))
                    .mapToInt(Integer::parseInt)
                    .max()
                    .orElse(1);
        } catch (IOException e) {
            log.error("Failed to find total pages", e);
            return 1;
        }
    }

    private ScrapedProductDto parseProductCard(Element product) {
        Element nameEl     = product.selectFirst("a[title]");
        Element priceEl    = product.selectFirst(".price");
        Element imgEl      = product.selectFirst("img");
        Element discountEl = product.selectFirst(".discount-product-floating");

        String rawPrice   = priceEl != null ? priceEl.text() : "0";
        String cleanPrice = rawPrice
                .replaceAll("[^0-9.]", "")
                .replaceAll("\\.(?=.*\\.)", "");

        String relativeUrl = nameEl != null ? nameEl.attr("href") : "";
        String fullUrl = relativeUrl.startsWith("http")
                ? relativeUrl
                : BASE_URL + "/" + relativeUrl;

        return ScrapedProductDto.builder()
                .productId(product.attr("data-id"))
                .name(nameEl != null
                        ? nameEl.attr("title").replace("\"", "").trim()
                        : null)
                .productUrl(fullUrl)
                .imageUrl(imgEl != null ? imgEl.attr("data-realpic") : null)
                .price(new BigDecimal(cleanPrice.isEmpty() ? "0" : cleanPrice))
                .currency("RON")
                .discount(discountEl != null ? discountEl.text().trim() : null)
                .category("Mountain Bike")
                .sourceWebsite("bikexpert.ro")
                .inStock(product.hasClass("stocid2"))
                .build();
    }

    private Document connect(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .header("Accept",
                        "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "ro-RO,ro;q=0.9")
                .header("Cache-Control", "no-cache")
                .followRedirects(true)
                .timeout(15_000)
                .get();
    }

    private void randomDelay() {
        try {
            Thread.sleep(500 + random.nextInt(1000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
