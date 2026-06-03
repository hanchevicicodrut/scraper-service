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
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SportguruScraperService {
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/120.0.0.0 Safari/537.36";

    private static final String BASE_URL    = "https://www.sportguru.ro";
    private static final String LISTING_URL = BASE_URL +
            "/sporturi/ciclism/biciclete/biciclete-pentru-munte-mtb";

    public List<ScrapedProductDto> scrapeListingPage() {
        log.info("Starting scrape: {}", LISTING_URL);
        List<ScrapedProductDto> results = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(LISTING_URL)
                    .userAgent("Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Mobile Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                    .header("Accept-Encoding", "gzip, deflate, br, zstd")
                    .header("Accept-Language", "ro,en-US;q=0.9,en;q=0.8")
                    .header("Cache-Control", "no-cache")
                    .header("Pragma", "no-cache")
                    .header("Sec-Ch-Ua", "\"Google Chrome\";v=\"137\", \"Chromium\";v=\"137\", \"Not(A:Brand\";v=\"24\"")
                    .header("Sec-Ch-Ua-Mobile", "?1")
                    .header("Sec-Ch-Ua-Platform", "\"Android\"")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "none")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("Priority", "u=0, i")
                    .header("Cookie","\n" +
                            "_fbp=fb.1.1730814502768.634635581899645226; __kla_id=eyJjaWQiOiJZelJqTWpObU9HTXRNV00xTVMwMFlXVmlMVGxpTkRNdE56azFOemxtT1dSbVlUbGkiLCIkcmVmZXJyZXIiOnsidHMiOjE3MzA4MTQ1NDUsInZhbHVlIjoiaHR0cHM6Ly93d3cuc3BvcnRndXJ1LnJvLz9xPWJldGUiLCJmaXJzdF9wYWdlIjoiaHR0cHM6Ly93d3cuc3BvcnRndXJ1LnJvL2JldGUtdHJla2tpbmctY2FyYm9uLXNpbHZhIn0sIiRsYXN0X3JlZmVycmVyIjp7InRzIjoxNzU0OTkxODMwLCJ2YWx1ZSI6Imh0dHBzOi8vd3d3Lmdvb2dsZS5jb20vIiwiZmlyc3RfcGFnZSI6Imh0dHBzOi8vd3d3LnNwb3J0Z3VydS5yby9jZWFzLWdhcm1pbi1mb3JlcnVubmVyLTk3MC1hbW9sZWQ/Z2FkX3NvdXJjZT0xJmdhZF9jYW1wYWlnbmlkPTE5NTYwMzUyNTExJmdicmFpZD0wQUFBQUFEeWxJZlRFckZGR3diQ3RZS3hiSlB1d2RtdmFMJmdjbGlkPUNqMEtDUWp3ek92RUJoRFZBUklzQURIZkpKUjlSVTRKQmxSQ2Z6dFJGV1RVOXdybFpLNzlVTTY0VjRDMmgtdi1JdzlJX1gyLVNPTnYxSk1hQXBfZEVBTHdfd2NCIyJ9fQ==; _clck=156xjdc%7C2%7Cfye%7C0%7C1903; CookieConsent={stamp:%27XiHx1Iic8u4X0w21bhgxoDj3X+bqsYRKnQhxhcuONnXEHwfMrkugsw==%27%2Cnecessary:true%2Cpreferences:true%2Cstatistics:true%2Cmarketing:true%2Cmethod:%27explicit%27%2Cver:1%2Cutc:1763383302904%2Cregion:%27ro%27}; _ga=GA1.1.57467058.1730814546; __sm__c={\"did\":\"933af4ca-bef5-470e-818e-0c976f23f71c\",\"k\":\"RTYQX9UP\"}; PHPSESSID=67b0g9cjab7f7aqn8ocd3hr6qq; form_key=N23IIrqmeBEvOF2j; mage-cache-sessid=true; _fbc=fb.1.1775545416944.IwVERTSARAi6lleHRuA2FlbQEwAGFkaWQBqy7BAG4WdXNydGMGYXBwX2lkDDM1MDY4NTUzMTcyOAABHpG1WhcrhiIugiJNdtrWnFLZC6C-5d7mgQ0hSg0oEK4L9wewx_rl6-LpWkEw_aem_kHwpaV0NfvH7trDDTu4-gg; mage-cache-storage={}; mage-cache-storage-section-invalidation={}; recently_viewed_product={}; recently_viewed_product_previous={}; recently_compared_product={}; recently_compared_product_previous={}; product_data_storage={}; _gcl_au=1.1.228637295.1779803357; amcookie_policy_restriction=denied; mage-messages=; section_data_ids={%22apptrian_metapixelapi_matching_section%22:1780382967%2C%22compare-products%22:1780042244}; private_content_version=38a52ab057ebc8a8229ee777d3f52177; cf_clearance=shnwtqK9xHFlrYiM0J05CIPX5m1bZCyOXdwmgDbisVQ-1780389007-1.2.1.1-ZC3WlRK0OV.bRMeuEAYqqwaY9JGUtrV_pgg7fsVRUxQ78d2cFcxKG7T64QHlyK6bOtuli15FjOM303Mvu2JqfHn9Oj_.nqYRsyqgQOW4qDHc4XITl_veGrIVO1SwP8xRDzW5k5fSmrmCQU9jP8XU5nrAddGzNyIRgNk1JPukCRQys2ds.sukbWNtSCZZ2iwKwiaAVjMWSfA6XLnQJF70aIk4zQDZDKlKeXZa7k_5X5QW5BNPy2VRIZb.dWIQLs87yl2Z7X57T.PqYbx.Fu7uPzsyKUd5uqU5pujoAU1J36nVNQJqS501pvafLSobJ8Q7Bd40OPR5khpSn9ObrqLsXA; _ga_RX4N3DTFME=GS2.1.s1780389008$o18$g0$t1780389008$j60$l0$h0; _ga_PTYLQFN4HE=GS2.1.s1780389008$o18$g0$t1780389008$j60$l0$h1360793869")
                    .followRedirects(true)
                    .timeout(15_000)
                    .get();

            log.info("Page fetched: {}", doc.title());

            // Select all product cards
            Elements productItems = doc.select("li.product-item");
            log.info("Products found on page: {}", productItems.size());

            for (Element item : productItems) {
                try {
                    ScrapedProductDto dto = parseProductCard(item);
                    results.add(dto);
                    log.info("Parsed: [{}] {} | {} {} | {}",
                            dto.getSku(),
                            dto.getName(),
                            dto.getPrice(),
                            dto.getCurrency(),
                            dto.getInStock() ? "IN STOCK" : "OUT OF STOCK"
                    );
                } catch (Exception e) {
                    log.warn("Failed to parse product item: {}", e.getMessage());
                }
            }

            log.info("Scrape complete. Total parsed: {}", results.size());

        } catch (IOException e) {
            log.error("Failed to fetch listing page: {}", LISTING_URL, e);
        }

        return results;
    }

    private ScrapedProductDto parseProductCard(Element item) {
        // All key data is in the anchor's data-* attributes
        Element anchor = item.selectFirst("a.product-item-photo");
        Element nameEl = item.selectFirst("a.product-item-link");
        Element priceEl = item.selectFirst("[data-price-amount]");
        Element imgEl   = item.selectFirst("img.product-image-photo");

        String priceRaw = priceEl != null
                ? priceEl.attr("data-price-amount") : "0";

        String stockRaw = anchor != null
                ? anchor.attr("data-dimension10") : "";

        return ScrapedProductDto.builder()
                .productId(item.attr("data-product-id"))
                .sku(anchor != null ? anchor.attr("data-id") : null)
                .name(nameEl != null ? nameEl.text() : null)
                .productUrl(anchor != null ? anchor.attr("href") : null)
                .imageUrl(imgEl != null ? imgEl.attr("src") : null)
                .price(new BigDecimal(priceRaw))
                .currency(anchor != null ? anchor.attr("data-currency") : "RON")
                .category(anchor != null ? anchor.attr("data-category") : null)
                .brand(anchor != null ? anchor.attr("data-brand") : null)
                .inStock("In stoc".equalsIgnoreCase(stockRaw))
                .sourceWebsite("sportguru.ro")
                .build();
    }
}
