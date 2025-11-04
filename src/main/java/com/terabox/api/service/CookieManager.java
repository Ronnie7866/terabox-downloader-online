package com.terabox.api.service;

import com.terabox.api.config.TeraBoxProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

/**
 * Service to manage cookie rotation with random selection
 */
@Slf4j
@Service
public class CookieManager {
    
    private final List<String> cookies;
    private final Random random;
    
    public CookieManager(TeraBoxProperties properties) {
        this.cookies = properties.getCookies();
        this.random = new Random();
        
        if (cookies == null || cookies.isEmpty()) {
            log.warn("No cookies configured! Please add cookies in application.properties");
        } else {
            log.info("Initialized CookieManager with {} cookies", cookies.size());
        }
    }
    
    /**
     * Get a random cookie from the pool
     */
    public String getRandomCookie() {
        if (cookies == null || cookies.isEmpty()) {
            log.error("No cookies available!");
            return "";
        }
        
        int index = random.nextInt(cookies.size());
        String cookie = cookies.get(index);
        log.debug("Selected cookie at index {} (total: {})", index, cookies.size());
        return cookie;
    }
    
    /**
     * Get total number of cookies
     */
    public int getCookieCount() {
        return cookies != null ? cookies.size() : 0;
    }
}

