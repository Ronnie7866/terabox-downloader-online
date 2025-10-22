package com.terabox.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration properties for TeraBox API
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "terabox")
public class TeraBoxProperties {
    
    private List<String> cookies;
    
    private HttpConfig http = new HttpConfig();
    
    private RetryConfig retry = new RetryConfig();
    
    @Data
    public static class HttpConfig {
        private int connectTimeout = 30000;
        private int readTimeout = 60000;
        private int writeTimeout = 60000;
    }
    
    @Data
    public static class RetryConfig {
        private int maxAttempts = 3;
        private long backoffDelay = 2000;
    }
}

