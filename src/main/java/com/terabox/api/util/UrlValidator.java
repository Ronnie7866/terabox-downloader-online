package com.terabox.api.util;

import java.util.regex.Pattern;

/**
 * Utility class for validating TeraBox URLs
 */
public class UrlValidator {
    
    private static final String[] URL_PATTERNS = {
        "ww\\.mirrobox\\.com", "www\\.nephobox\\.com", "freeterabox\\.com",
        "www\\.freeterabox\\.com", "1024tera\\.com", "4funbox\\.co",
        "www\\.4funbox\\.com", "mirrobox\\.com", "nephobox\\.com",
        "terabox\\.app", "terabox\\.com", "www\\.terabox\\.ap",
        "www\\.terabox\\.com", "www\\.1024tera\\.co", "www\\.momerybox\\.com",
        "teraboxapp\\.com", "momerybox\\.com", "tibibox\\.com",
        "www\\.tibibox\\.com", "www\\.teraboxapp\\.com", "www\\.teraboxlink\\.com",
        "teraboxlink\\.com", "www\\.terasharelink\\.com", "terasharelink\\.com",
        "www\\.terafileshare\\.com", "terafileshare\\.com", "www\\.teraboxshare\\.com",
        "teraboxshare\\.com", "www\\.terabox\\.club", "terabox\\.club",
        "www\\.teraboxurl\\.com", "teraboxurl\\.com", "www\\.terasharefile\\.com",
        "terasharefile\\.com", "www\\.terabox\\.fun", "terabox\\.fun",
        "www\\.gibibox\\.com", "gibibox\\.com", "www\\.terabox1024\\.com",
        "terabox1024\\.com"
    };
    
    /**
     * Validate if the URL is a legitimate TeraBox URL
     */
    public static boolean isValidTeraBoxUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        
        for (String pattern : URL_PATTERNS) {
            if (Pattern.compile(pattern).matcher(url).find()) {
                return true;
            }
        }
        
        return false;
    }
}

