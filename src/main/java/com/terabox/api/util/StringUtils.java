package com.terabox.api.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for string parsing operations
 */
public class StringUtils {
    
    /**
     * Find text between two strings
     */
    public static String findBetween(String data, String first, String last) {
        try {
            int start = data.indexOf(first);
            if (start == -1) {
                return null;
            }
            start += first.length();
            
            int end = data.indexOf(last, start);
            if (end == -1) {
                return null;
            }
            
            return data.substring(start, end);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Extract surl from TeraBox URL
     * Handles various URL formats including direct shares and embed links
     */
    public static String extractSurl(String url) {
        try {
            // Remove leading '1' if present
            if (url.startsWith("1")) {
                url = url.substring(1);
            }
            
            // First try to get surl from query parameters (embed URLs)
            Pattern queryPattern = Pattern.compile("[?&]surl=([A-Za-z0-9_-]+)");
            Matcher queryMatcher = queryPattern.matcher(url);
            if (queryMatcher.find()) {
                String surl = queryMatcher.group(1);
                return surl;
            }
            
            // Then try to extract from path (direct share URLs)
            Pattern pathPattern = Pattern.compile("/s/([A-Za-z0-9_-]+)");
            Matcher pathMatcher = pathPattern.matcher(url);
            if (pathMatcher.find()) {
                String surl = pathMatcher.group(1);
                // Remove leading '1' from surl if present
                if (surl.startsWith("1")) {
                    surl = surl.substring(1);
                }
                return surl;
            }
            
            // Try to extract URLs that are just the code or already in surl format
            Pattern directPattern = Pattern.compile("([A-Za-z0-9_-]+)");
            Matcher directMatcher = directPattern.matcher(url);
            if (directMatcher.find()) {
                String surl = directMatcher.group(0);
                // Remove leading '1' from surl if present
                if (surl.startsWith("1")) {
                    surl = surl.substring(1);
                }
                return surl;
            }
            
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * URL decode a string
     */
    public static String urlDecode(String value) {
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }
    
    /**
     * Extract parameter from URL
     */
    public static String extractUrlParameter(String url, String paramName) {
        try {
            Pattern pattern = Pattern.compile("[?&]" + paramName + "=([^&]+)");
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                return matcher.group(1);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}

