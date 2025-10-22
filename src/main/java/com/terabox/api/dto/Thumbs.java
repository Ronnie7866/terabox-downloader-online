package com.terabox.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Thumbnail URLs in TeraBox response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Thumbs {
    
    @JsonProperty("url1")
    private String url1;
    
    @JsonProperty("url2")
    private String url2;
    
    @JsonProperty("url3")
    private String url3;
    
    @JsonProperty("icon")
    private String icon;
}

