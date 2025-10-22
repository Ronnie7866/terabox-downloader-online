package com.terabox.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Main Spring Boot Application for TeraBox API
 */
@Slf4j
@SpringBootApplication
@EnableConfigurationProperties
public class TeraBoxApiApplication {
    
    public static void main(String[] args) {
        log.info("Starting TeraBox API Spring Boot Application...");
        SpringApplication.run(TeraBoxApiApplication.class, args);
        log.info("TeraBox API Application started successfully!");
    }
}

