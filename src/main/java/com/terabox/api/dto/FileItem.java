package com.terabox.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * File item in TeraBox response matching official API structure
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileItem {
    
    @JsonProperty("category")
    private String category;
    
    @JsonProperty("fs_id")
    private String fsId;
    
    @JsonProperty("isdir")
    private String isdir;
    
    @JsonProperty("local_ctime")
    private String localCtime;
    
    @JsonProperty("local_mtime")
    private String localMtime;
    
    @JsonProperty("md5")
    private String md5;
    
    @JsonProperty("path")
    private String path;
    
    @JsonProperty("play_forbid")
    private String playForbid;
    
    @JsonProperty("server_ctime")
    private String serverCtime;
    
    @JsonProperty("server_filename")
    private String serverFilename;
    
    @JsonProperty("server_mtime")
    private String serverMtime;
    
    @JsonProperty("size")
    private String size;
    
    @JsonProperty("thumbs")
    private Thumbs thumbs;
    
    @JsonProperty("emd5")
    private String emd5;
    
    @JsonProperty("dlink")
    private String dlink;
    
    @JsonProperty("direct_link")
    private String directLink;
    
    @JsonProperty("stream_url")
    private String streamUrl;
}

