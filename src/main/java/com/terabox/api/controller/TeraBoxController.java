package com.terabox.api.controller;

import com.terabox.api.dto.ErrorResponse;
import com.terabox.api.dto.TeraBoxRequest;
import com.terabox.api.dto.TeraBoxResponse;
import com.terabox.api.service.TeraBoxExtractor;
import com.terabox.api.util.UrlValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for TeraBox API
 */
@Slf4j
@RestController
@RequestMapping("/v2")
@RequiredArgsConstructor
public class TeraBoxController {
    
    private final TeraBoxExtractor teraBoxExtractor;
    
    /**
     * Main endpoint to process TeraBox URLs
     * 
     * POST /api/terabox
     * Body: { "url": "terabox_url" }
     * 
     * Returns: Official TeraBox API response structure
     */
    @PostMapping("/api")
    public ResponseEntity<?> processTeraBoxUrl(@RequestBody TeraBoxRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Validate request
            if (request == null || request.getUrl() == null || request.getUrl().trim().isEmpty()) {
                log.warn("Empty URL provided in request");
                return buildErrorResponse(
                        -1,
                        "URL is required",
                        HttpStatus.BAD_REQUEST
                );
            }
            
            String url = request.getUrl().trim();
            log.info("Received request for URL: {}", url);
            
            // Validate TeraBox URL
            if (!UrlValidator.isValidTeraBoxUrl(url)) {
                log.warn("Invalid TeraBox URL: {}", url);
                return buildErrorResponse(
                        -1,
                        "Invalid TeraBox URL. Please provide a valid TeraBox share link.",
                        HttpStatus.BAD_REQUEST
                );
            }
            
            // Process URL
            TeraBoxResponse response = teraBoxExtractor.getData(url);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("Successfully processed URL in {} ms. Files: {}", duration, response.getList().size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Error processing TeraBox URL after {} ms: {}", duration, e.getMessage(), e);
            
            return buildErrorResponse(
                    -1,
                    "Failed to process TeraBox URL: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok()
                .body(new HealthResponse("OK", "TeraBox API is running"));
    }
    
    /**
     * Build error response
     */
    private ResponseEntity<?> buildErrorResponse(int errno, String errorMessage, HttpStatus status) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errno(errno)
                .error(errorMessage)
                .requestId(System.currentTimeMillis() * 1000)
                .serverTime(System.currentTimeMillis() / 1000)
                .build();
        
        return ResponseEntity.status(status).body(errorResponse);
    }
    
    /**
     * Simple health response class
     */
    private static class HealthResponse {
        public String status;
        public String message;
        
        public HealthResponse(String status, String message) {
            this.status = status;
            this.message = message;
        }
    }
}

