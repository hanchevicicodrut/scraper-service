package com.chan.scraper_service.enums;

public enum ScrapeStatus {
    RUNNING,    // currently scraping
    SUCCESS,    // all products scraped ok
    PARTIAL,    // some failed, some ok
    FAILED      // complete failure
}
