package com.chan.scraper_service.enums;

public enum PriceChangeType {
    FIRST_SEEN,      // product scraped for the first time
    PRICE_UP,        // price increased
    PRICE_DOWN,      // price decreased 🎯 most interesting for users
    BACK_IN_STOCK,   // was out of stock, now available
    OUT_OF_STOCK,    // was available, now out of stock
    SIZES_CHANGED,   // available sizes changed
    NO_CHANGE        // price same, stock same, sizes same (rarely stored)
}
