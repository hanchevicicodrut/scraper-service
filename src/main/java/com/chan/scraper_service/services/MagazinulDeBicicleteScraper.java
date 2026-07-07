package com.chan.scraper_service.services;

import com.chan.scraper_service.dtos.ScrapedProductDto;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MagazinulDeBicicleteScraper {

    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/137.0.0.0 Safari/537.36";

    private static final String BASE_URL = "https://www.magazinuldebiciclete.ro";
    private static final String SOURCE = "magazinuldebiciclete.ro";

    private static final List<String> CATEGORIES = List.of(
             "/biciclete/electrice",
             "/biciclete/biciclete-sosea",
             "/biciclete/biciclete-enduro",
             "/biciclete/biciclete-mountain-bike",
             "/biciclete/biciclete-trekking",
             "/biciclete/biciclete-copii"
     );


    private final Random random = new Random();

    // ─────────────────────────────────────────────────────────────
    // PUBLIC API
    // ─────────────────────────────────────────────────────────────

    @Retryable(
            retryFor = IOException.class,
            noRetryFor = HttpStatusException.class,
            backoff = @Backoff(delay = 5000, multiplier = 2, maxDelay = 30000)
    )
    public List<ScrapedProductDto> scrapeAllPages() throws IOException {
        List<ScrapedProductDto> allProducts = new ArrayList<>();

        for (String categoryPath : CATEGORIES) {
            log.info("━━━ Scraping category: {}", categoryPath);
            List<ScrapedProductDto> products = scrapeCategory(categoryPath);
            allProducts.addAll(products);
            randomDelay();
        }

        log.info("━━━ Scrape complete. Total available products: {}",
                allProducts.size());
        return allProducts;
    }

    @Recover
    public List<ScrapedProductDto> recoverScrapeAllPages(IOException e) {
        log.error("💀 scrapeAllPages failed after all retries: {}", e.getMessage());
        return Collections.emptyList();
    }

    // ─────────────────────────────────────────────────────────────
    // PRIVATE — CATEGORY SCRAPING
    // ─────────────────────────────────────────────────────────────

    public List<ScrapedProductDto> scrapeCategory(String categoryPath)
            throws IOException {
        List<ScrapedProductDto> results = new ArrayList<>();
        String url = BASE_URL + categoryPath;

        // IOException propagates up to @Retryable
        Document doc = connect(url, "scrape category");
        log.info("Page fetched: {} | title: {}", url, doc.title());
        //dumpHtmlToFile(categoryPath, doc.outerHtml());

        CategoryData categoryData = extractCategoryAndSubcategory(doc);
        log.info("Category: {} | Subcategory: {}",
                categoryData.category(), categoryData.subcategory());

        Elements items = doc.select("#pgrid div.hs-item");
        log.info("Products found on page: {}", items.size());
//
//        Set<String> seenUrls = new HashSet<>();

        for (Element item : items) {

            try {
                ScrapedProductDto dto = parseProductCard(item);
                if (dto == null) continue;
//                if (!seenUrls.add(dto.getProductUrl())) {
//                    log.debug("Skipping duplicate product: {}", dto.getProductUrl());
//                    continue;
//                }

                dto.setCategory(categoryData.category());
                dto.setSubcategory(categoryData.subcategory());

                enrichWithDetailPage(dto);
                randomDelay();

                if (Boolean.TRUE.equals(dto.getInStock())) {
                    results.add(dto);
                }
            } catch (Exception e) {
                log.warn("Failed to parse product: {}", e.getMessage());
            }
        }

        return results;
    }

    // ─────────────────────────────────────────────────────────────
    // PRIVATE — PARSE PRODUCT CARD
    // ─────────────────────────────────────────────────────────────

    private ScrapedProductDto parseProductCard(Element item) {
        log.debug("parseProductCard - START");
        // ── URL ───────────────────────────────────────────────────
        Element linkEl = item.selectFirst("div.img-wrap a");
        String relUrl = linkEl != null ? linkEl.attr("href") : "";
        String fullUrl = relUrl.startsWith("http")
                ? relUrl : BASE_URL + relUrl;

        // ── PRODUCT ID ────────────────────────────────────────────
        Element compareEl = item.selectFirst("[data-productid]");
        String productId = compareEl != null
                ? compareEl.attr("data-productid") : null;

        // ── IMAGE ─────────────────────────────────────────────────
        Element imgEl = item.selectFirst("div.img-wrap img");
        String imageUrl = imgEl != null
                ? BASE_URL + imgEl.attr("src") : null;

        // ── NAME — fallback chain ─────────────────────────────────
        String name = null;

        // 1. div.feat_menu_it (most products on this site)
        Element featEl = item.selectFirst("div.feat_menu_it");
        if (featEl != null && !featEl.text().isBlank()) {
            name = featEl.text().trim();
        }
        // 2. h2.title (some products)
        if (name == null) {
            Element nameEl = item.selectFirst("h2.title");
            if (nameEl != null && !nameEl.text().isBlank()) {
                name = nameEl.text().trim();
            }
        }
        // 3. img alt fallback
        if (name == null && imgEl != null
                && !imgEl.attr("alt").isBlank()) {
            name = imgEl.attr("alt").trim();
        }
        // 4. img title fallback
        if (name == null && imgEl != null
                && !imgEl.attr("title").isBlank()) {
            name = imgEl.attr("title").trim();
        }

        if (name == null || name.isBlank()) {
            log.warn("⚠️ Could not extract name for productId={} url={}",
                    productId, fullUrl);
        }

        // ── PRICE ─────────────────────────────────────────────────
        Element priceEl = item.selectFirst("span.new strong");
        BigDecimal price = extractPrice(priceEl);

        // ── ORIGINAL PRICE ────────────────────────────────────────
        BigDecimal originalPrice = null;
        Element oldPriceEl = item.selectFirst("span.old strike");
        if (oldPriceEl != null) {
            originalPrice = extractPriceFromText(oldPriceEl.text());
        }

        // ── DISCOUNT ──────────────────────────────────────────────
        Element discountEl = item.selectFirst("[class*=option-reducere]");
        String discount = null;
        if (discountEl != null
                && !discountEl.classNames().contains("hidden")) {
            discount = discountEl.text().trim();
        }
//        log.debug("━━━ Parsed product card ━━━━━━━━━━━━━━━━━━━━━━━━━━");
//        log.debug("  content:       {}", item.outerHtml());
//        log.debug("  productId:     {}", productId);
//        log.debug("  name:          {}", name);
//        log.debug("  relUrl:        {}", relUrl);
//        log.debug("  fullUrl:       {}", fullUrl);
//        log.debug("  imageUrl:      {}", imageUrl);
//        log.debug("  price:         {}", price);
//        log.debug("  originalPrice: {}", originalPrice);
//        log.debug("  discount:      {}", discount);
//        log.debug("  currency:      RON");
//        log.debug("  sourceWebsite: {}", SOURCE);
//        log.debug("  inStock:       true (refined later)");
//        log.debug("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        return ScrapedProductDto.builder()
                .productId(productId)
                .name(name)
                .productUrl(fullUrl)
                .imageUrl(imageUrl)
                .price(price)
                .originalPrice(originalPrice)
                .currency("RON")
                .discount(discount)
                .sourceWebsite(SOURCE)
                .inStock(true)
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // PRIVATE — ENRICH WITH DETAIL PAGE
    // ─────────────────────────────────────────────────────────────

    public void enrichWithDetailPage(ScrapedProductDto dto) {
        if (dto.getProductUrl() == null) return;

        try {
            Document doc = connect(dto.getProductUrl(), "enrich");


            // ── SCOPE TO #product-info ONLY ───────────────────────
            Element productInfo = doc.selectFirst("#product-info");
            if (productInfo == null) {
                log.warn("⚠️ #product-info not found: {}", dto.getProductUrl());
                return;
            }
//            log.debug("━━━ Detail page: {} ━━━", dto.getProductUrl());
//            log.debug("  pid:       {}", doc.selectFirst(".pid") != null
//                    ? doc.selectFirst(".pid").text() : "NULL");
//            log.debug("  price:     {}", productInfo.selectFirst("div.new strong") != null
//                    ? productInfo.selectFirst("div.new strong").text() : "NULL");
//            log.debug("  stockZero: {}", productInfo.selectFirst(".stock_zero") != null
//                    ? productInfo.selectFirst(".stock_zero").text() : "NULL");
//            log.debug("  stockOk:   {}", productInfo.selectFirst(".stock_ok") != null
//                    ? productInfo.selectFirst(".stock_ok").text() : "NULL");
//            log.debug("  brandDiv:  {}", productInfo.selectFirst("#brandDiv") != null
//                    ? productInfo.selectFirst("#brandDiv").outerHtml() : "NULL");
//            log.debug("  desc:      {}", productInfo.selectFirst(".description p") != null
//                    ? productInfo.selectFirst(".description p").text() : "NULL");

            // ── SKU — search full doc, pid is OUTSIDE #product-info ──
            Element skuEl = doc.selectFirst(".pid");
            if (skuEl != null) {
                String rawSku = skuEl.text();
                log.debug("  SKU raw: '{}'", rawSku);
                String sku = rawSku
                        .replaceAll("(?i)cod\\s*produs\\s*:", "")
                        .replaceAll("(?i)cod\\s*:", "")
                        .trim();
                log.debug("  SKU cleaned: '{}'", sku);
                dto.setSku(sku);
            }

            // ── CURRENT PRICE — scoped to productInfo ────────────────
            Element detailPriceEl = productInfo.selectFirst("div.new strong");
            if (detailPriceEl != null) {
                BigDecimal detailPrice = extractPrice(detailPriceEl);
                if (detailPrice.compareTo(BigDecimal.ZERO) > 0) {
                    dto.setPrice(detailPrice);
                }
            }

            // ── STOCK — scoped to productInfo ────────────────────────
//            Element stockZero = productInfo.selectFirst(".stock_zero");
//            Element stockOk   = productInfo.selectFirst(".stock_ok");
//
//            if (stockZero != null) {
//                dto.setInStock(false);
//                log.info("  ⏭️  Out of stock — skipping: {}", dto.getName());
//                return;
//            }
            //dto.setInStock(stockOk != null || stockZero == null);
            dto.setInStock(true);

            // ── BRAND ─────────────────────────────────────────────
            Element brandEl = doc.selectFirst("#brandDiv img");
            if (brandEl != null) {
                dto.setBrand(brandEl.attr("alt").trim());
            }

            // ── DESCRIPTION ───────────────────────────────────────
            // ── DESCRIPTION — in #descriere-produs tab, outside #product-info ──
            Element descEl = doc.selectFirst("#descriere-produs .tab-content");
            if (descEl != null) {
                dto.setDescription(descEl.text().trim());
            }

            // ── SPECIFICATIONS TABLE → attributes ─────────────────
            Map<String, String> attributes = new LinkedHashMap<>();
            List<String> sizes = new ArrayList<>();

            Elements rows = doc.select("table tr");
            for (Element row : rows) {
                Elements cols = row.select("td");

                if (cols.size() < 2) continue;
                if (cols.get(0).hasAttr("colspan")) continue;

                String key = cols.get(0).text().trim();
                String value = cols.get(1).text()
                        .replaceAll("\\s+", " ")
                        .trim();

                if (key.isEmpty() || value.isEmpty()) continue;

                attributes.put(key, value);

                // Extract sizes
                if (key.equalsIgnoreCase("Marimi")
                        || key.equalsIgnoreCase("Marime")) {
                    sizes = Arrays.stream(value.split("[,;/]"))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toList());
                }

                // Extract brand from specs fallback
                /*if ((key.equalsIgnoreCase("Producator")
                        || key.equalsIgnoreCase("Brand"))
                        && dto.getBrand() == null) {
                    dto.setBrand(value);
                }*/
                dto.setBrand("Giant");
            }

            if (!sizes.isEmpty()) dto.setAvailableSizes(sizes);
            dto.setAttributes(attributes);

            log.info("  ✅ [{}] {} | brand={} | stock={} | sizes={} | attrs={}",
                    dto.getSku(),
                    dto.getName(),
                    dto.getBrand(),
                    dto.getInStock(),
                    sizes,
                    attributes.size());

        } catch (Exception e) {
            log.warn("  ⚠️  Failed to enrich: {} → {}",
                    dto.getProductUrl(), e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────

    private record CategoryData(String category, String subcategory) {
    }

    private CategoryData extractCategoryAndSubcategory(Document doc) {
        String category = null;
        String subcategory = null;

        // Breadcrumb: Home Page > Produse > Biciclete > Biciclete Electrice
        //                  0          1         2              3
        Elements breadcrumbs = doc.select("#breadcrumbs ul li a");

        if (breadcrumbs.size() >= 3) {
            category = breadcrumbs.get(2).text().trim();
        }
        if (breadcrumbs.size() >= 4) {
            subcategory = breadcrumbs.get(3).text().trim();
        }

        // Fallback: last breadcrumb item
        if (subcategory == null) {
            Element lastLi = doc.selectFirst("#breadcrumbs ul li.last a");
            if (lastLi != null) subcategory = lastLi.text().trim();
        }

        log.info("Category: {} | Subcategory: {}", category, subcategory);
        return new CategoryData(category, subcategory);
    }

    private BigDecimal extractPrice(Element priceEl) {
        if (priceEl == null) return BigDecimal.ZERO;

        Element supEl = priceEl.selectFirst("sup");

        String wholePart = priceEl.textNodes().stream()
                .map(tn -> tn.text().trim())
                .filter(t -> !t.isEmpty())
                .findFirst()
                .orElse("0");

        // sup contains: <span class="priceComma">,</span>00
        // we need only the text node "00" inside sup, not the span
        String decimalPart = "00";
        if (supEl != null) {
            decimalPart = supEl.textNodes().stream()
                    .map(tn -> tn.text().trim())
                    .filter(t -> !t.isEmpty())
                    .findFirst()
                    .orElse("00");
        }

        String cleanWhole = wholePart.replaceAll("[^0-9]", "");
        String cleanDecimal = decimalPart.replaceAll("[^0-9]", "");

        if (cleanWhole.isEmpty()) return BigDecimal.ZERO;
        if (cleanDecimal.isEmpty()) cleanDecimal = "00";
        else if (cleanDecimal.length() == 1) cleanDecimal = cleanDecimal + "0";
        else if (cleanDecimal.length() > 2) cleanDecimal = "00";

        return new BigDecimal(cleanWhole + "." + cleanDecimal);
    }

    /**
     * Dumps the raw fetched HTML to target/debug-html so it can be diffed
     * against what the browser actually renders (e.g. to spot products
     * that are present in the server response but hidden/filtered client-side).
     */
    private void dumpHtmlToFile(String label, String html) {
        try {
            Path dir = Path.of("target", "debug-html");
            Files.createDirectories(dir);
            String safeName = label.replaceAll("[^a-zA-Z0-9-]+", "_") + ".html";
            Path file = dir.resolve(safeName);
            Files.writeString(file, html);
            log.info("Dumped fetched HTML to {}", file.toAbsolutePath());
        } catch (IOException e) {
            log.warn("Failed to dump HTML for {}: {}", label, e.getMessage());
        }
    }

    private Document connect(String url, String sursa) throws IOException {
        log.debug("connect() - url: {}, sursa: {}", url, sursa);
        return Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .header("Accept",
                        "text/html,application/xhtml+xml,*/*;q=0.8")
                .header("Accept-Language", "ro-RO,ro;q=0.9")
                .header("Cache-Control", "no-cache")
                .followRedirects(true)
                .timeout(35_000)
                .get();
    }

    private void randomDelay() {
        try {
            Thread.sleep(1000 + random.nextInt(2000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private BigDecimal extractPriceFromText(String text) {
        if (text == null || text.isBlank()) return null;
        try {
            String clean = text
                    .replaceAll("[^0-9,.]", "")  // keep digits, comma, dot
                    .replace(".", "")             // remove thousand separators
                    .replace(",", ".");           // comma → decimal point
            return clean.isEmpty() ? null : new BigDecimal(clean);
        } catch (NumberFormatException e) {
            log.warn("Failed to parse old price: {}", text);
            return null;
        }
    }
}
