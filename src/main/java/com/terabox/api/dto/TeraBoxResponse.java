package com.terabox.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO matching official TeraBox API structure
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeraBoxResponse {
    
    @JsonProperty("errno")
    private Integer errno;
    
    @JsonProperty("request_id")
    private Long requestId;
    
    @JsonProperty("server_time")
    private Long serverTime;
    
    @JsonProperty("cfrom_id")
    private Integer cfromId;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("list")
    private List<FileItem> list;
    
    @JsonProperty("share_id")
    private Long shareId;
    
    @JsonProperty("uk")
    private Long uk;
}

