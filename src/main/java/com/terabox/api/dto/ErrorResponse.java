package com.terabox.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Error response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    
    @JsonProperty("errno")
    private Integer errno;
    
    @JsonProperty("error")
    private String error;
    
    @JsonProperty("request_id")
    private Long requestId;
    
    @JsonProperty("server_time")
    private Long serverTime;
}

