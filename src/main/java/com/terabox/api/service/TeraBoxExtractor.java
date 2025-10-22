package com.terabox.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.terabox.api.config.TeraBoxProperties;
import com.terabox.api.dto.FileItem;
import com.terabox.api.dto.TeraBoxResponse;
import com.terabox.api.dto.Thumbs;
import com.terabox.api.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Core service to extract data from TeraBox URLs
 */
@Slf4j
@Service
public class TeraBoxExtractor {
    
    private final CookieManager cookieManager;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final TeraBoxProperties properties;
    private final Executor taskExecutor;

    public TeraBoxExtractor(CookieManager cookieManager, TeraBoxProperties properties, Executor teraboxTaskExecutor) {
        this.cookieManager = cookieManager;
        this.properties = properties;
        this.taskExecutor = teraboxTaskExecutor;
        this.objectMapper = new ObjectMapper();
        
        // Build OkHttp client with configured timeouts
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(properties.getHttp().getConnectTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(properties.getHttp().getReadTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(properties.getHttp().getWriteTimeout(), TimeUnit.MILLISECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .build();
    }
    
    /**
     * Main method to get data from TeraBox URL
     */
    public TeraBoxResponse getData(String url) throws Exception {
        log.info("Processing TeraBox URL: {}", url);
        
        // Step 1: Try to get redirected URL
        String redirectedUrl = getRedirectedUrl(url);
        log.info("Redirected URL: {}", redirectedUrl);
        
        String finalUrl;
        
        // Step 2: Decide which URL to use based on redirect
        if (redirectedUrl != null && !redirectedUrl.equals(url)) {
            // We got a redirect, use it directly
            log.info("Using redirected URL directly");
            finalUrl = redirectedUrl;
        } else {
            // No redirect, extract surl and build canonical URL
            log.info("No redirect found, extracting surl");
            String surl = StringUtils.extractSurl(url);
            if (surl != null) {
                finalUrl = "https://dm.1024tera.com/sharing/link?surl=" + surl + "&clearCache=1";
                log.info("Canonicalized URL: {}", finalUrl);
            } else {
                log.error("Failed to extract surl from URL");
                throw new Exception("Invalid TeraBox URL - cannot extract surl");
            }
        }
        
        // Step 3: Fetch data with retry logic
        return fetchDataWithRetry(finalUrl);
    }
    
    /**
     * Get redirected URL by checking Location header from HTTP redirect response
     * Uses full cookie string to match browser behavior
     */
    private String getRedirectedUrl(String url) {
        try {
            // Get a random cookie from the pool
            String cookie = cookieManager.getRandomCookie();

            Request request = new Request.Builder()
                    .url(url)
                    .header("Cookie", cookie)
                    .header("cache-control", "max-age=0")
                    .header("sec-ch-ua", "\"Google Chrome\";v=\"141\", \"Not?A_Brand\";v=\"8\", \"Chromium\";v=\"141\"")
                    .header("sec-ch-ua-mobile", "?0")
                    .header("sec-ch-ua-platform", "\"Windows\"")
                    .header("upgrade-insecure-requests", "1")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                    .header("sec-fetch-site", "none")
                    .header("sec-fetch-mode", "navigate")
                    .header("sec-fetch-user", "?1")
                    .header("sec-fetch-dest", "document")
                    .header("accept-encoding", "gzip, deflate, br, zstd")
                    .header("accept-language", "en-GB,en-US;q=0.9,en;q=0.8")
                    .header("priority", "u=0, i")
                    .build();

            // Create a client that does NOT follow redirects automatically
            // This allows us to capture the Location header from the redirect response
            OkHttpClient noRedirectClient = httpClient.newBuilder()
                    .followRedirects(false)
                    .followSslRedirects(false)
                    .build();

            try (Response response = noRedirectClient.newCall(request).execute()) {
                log.debug("Redirect check - Status: {}, URL: {}", response.code(), url);

                // Check if we got a redirect response (3xx status codes)
                if (response.isRedirect()) {
                    String location = response.header("Location");
                    if (location != null && !location.isEmpty()) {
                        log.info("Got redirect from Location header: {}", location);
                        return location;
                    } else {
                        log.warn("Got redirect status {} but no Location header", response.code());
                    }
                }

                // If no redirect, return the original URL
                log.debug("No redirect found, using original URL");
                return url;
            }
        } catch (Exception e) {
            log.warn("Error getting redirected URL: {}", e.getMessage());
            return url; // Return original URL if redirect fails
        }
    }
    
    /**
     * Fetch data with retry logic
     */
    private TeraBoxResponse fetchDataWithRetry(String url) throws Exception {
        int maxAttempts = properties.getRetry().getMaxAttempts();
        long backoffDelay = properties.getRetry().getBackoffDelay();
        
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                log.info("Attempt {}/{} to fetch data", attempt, maxAttempts);
                return fetchData(url);
            } catch (Exception e) {
                lastException = e;
                log.warn("Attempt {}/{} failed: {}", attempt, maxAttempts, e.getMessage());
                
                if (attempt < maxAttempts) {
                    long sleepTime = backoffDelay * (long) Math.pow(2, attempt - 1);
                    log.info("Retrying in {} ms...", sleepTime);
                    Thread.sleep(sleepTime);
                }
            }
        }
        
        throw new Exception("All retry attempts failed", lastException);
    }
    
    /**
     * Fetch data from TeraBox URL
     */
    private TeraBoxResponse fetchData(String url) throws Exception {
        // Get random cookie
        String cookie = cookieManager.getRandomCookie();
        
        // Step 1: Get HTML page to extract tokens
        Request pageRequest = new Request.Builder()
                .url(url)
                .header("Cookie", cookie)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .build();
        
        String htmlContent;
        String redirectUrl;
        
        try (Response response = httpClient.newCall(pageRequest).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to fetch page: HTTP " + response.code());
            }
            
            htmlContent = response.body().string();
            redirectUrl = response.request().url().toString();
            
            // Check if we're getting a login page
            if (htmlContent.toLowerCase().contains("login") && htmlContent.toLowerCase().contains("password")) {
                throw new Exception("Cookie is invalid - getting login page");
            }
        }
        
        // Step 2: Extract required tokens from HTML
        String logid = StringUtils.findBetween(htmlContent, "dp-logid=", "&");
        String jsToken = StringUtils.findBetween(htmlContent, "fn%28%22", "%22%29");
        String shorturl = StringUtils.extractSurl(redirectUrl);
        String defaultThumbnail = StringUtils.findBetween(htmlContent, "og:image\" content=\"", "\"");
        
        if (shorturl == null || jsToken == null) {
            throw new Exception("Failed to extract required tokens (shorturl or jsToken)");
        }
        
        log.debug("Extracted - logid: {}, jsToken: {}, shorturl: {}", logid, jsToken, shorturl);
        
        // Step 3: Get file list from API
        String apiUrl = String.format(
                "https://dm.1024tera.com/share/list?app_id=250528&web=1&channel=dubox&clienttype=0&jsToken=%s&dp-logid=%s&by=name&order=asc&site_referer=&shorturl=%s&root=1",
                jsToken, logid != null ? logid : "", shorturl
        );
        
        Request apiRequest = new Request.Builder()
                .url(apiUrl)
                .header("Cookie", cookie)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .header("Accept", "application/json, text/plain, */*")
                .header("Referer", url)
                .build();
        
        JsonNode apiResponse;
        try (Response response = httpClient.newCall(apiRequest).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to fetch file list: HTTP " + response.code());
            }
            
            String responseBody = response.body().string();
            apiResponse = objectMapper.readTree(responseBody);
        }
        
        // Check errno
        int errno = apiResponse.path("errno").asInt(-1);
        if (errno != 0) {
            throw new Exception("API returned error: errno=" + errno);
        }

        // Extract metadata from API response
        long requestId = apiResponse.path("request_id").asLong(System.currentTimeMillis() * 1000);
        long serverTime = apiResponse.path("server_time").asLong(System.currentTimeMillis() / 1000);
        int cfromId = apiResponse.path("cfrom_id").asInt(0);
        String title = apiResponse.path("title").asText("");
        long shareId = apiResponse.path("share_id").asLong(0);
        long uk = apiResponse.path("uk").asLong(0);

        // Step 4: Process files and folders in parallel
        JsonNode fileList = apiResponse.path("list");
        List<FileItem> processedFiles = processFilesAndFoldersParallel(fileList, shorturl, uk, shareId, jsToken, logid, defaultThumbnail, cookie);

        // Step 5: Build response using values from TeraBox API
        return TeraBoxResponse.builder()
                .errno(errno)
                .requestId(requestId)
                .serverTime(serverTime)
                .cfromId(cfromId)
                .title(title)
                .list(processedFiles)
                .shareId(shareId)
                .uk(uk)
                .build();
    }

    /**
     * Process files and folders in parallel for maximum performance
     */
    private List<FileItem> processFilesAndFoldersParallel(JsonNode fileList, String shorturl, long uk, long shareId,
                                                           String jsToken, String logid, String defaultThumbnail, String cookie) {
        if (!properties.getAsync().isEnabled()) {
            // Fallback to sequential processing if async is disabled
            return processFilesAndFoldersSequential(fileList, shorturl, uk, shareId, jsToken, logid, defaultThumbnail, cookie);
        }

        try {
            List<CompletableFuture<List<FileItem>>> futures = new ArrayList<>();

            // Create async tasks for each file/folder
            fileList.forEach(fileNode -> {
                CompletableFuture<List<FileItem>> future = CompletableFuture.supplyAsync(() -> {
                    if (fileNode.path("isdir").asText().equals("1")) {
                        // It's a folder - process recursively
                        return processFolder(fileNode, shorturl, uk, shareId, jsToken, logid, defaultThumbnail, cookie);
                    } else {
                        // It's a file - process it
                        FileItem fileItem = processFile(fileNode, uk, shareId, jsToken, defaultThumbnail, cookie);
                        List<FileItem> result = new ArrayList<>();
                        if (fileItem != null) {
                            result.add(fileItem);
                        }
                        return result;
                    }
                }, taskExecutor);

                futures.add(future);
            });

            // Wait for all tasks to complete and collect results
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

            return allFutures.thenApply(v ->
                futures.stream()
                    .map(CompletableFuture::join)
                    .flatMap(List::stream)
                    .collect(Collectors.toList())
            ).join();

        } catch (Exception e) {
            log.warn("Async processing failed, falling back to sequential: {}", e.getMessage());
            return processFilesAndFoldersSequential(fileList, shorturl, uk, shareId, jsToken, logid, defaultThumbnail, cookie);
        }
    }

    /**
     * Sequential fallback for processing files and folders
     */
    private List<FileItem> processFilesAndFoldersSequential(JsonNode fileList, String shorturl, long uk, long shareId,
                                                             String jsToken, String logid, String defaultThumbnail, String cookie) {
        List<FileItem> processedFiles = new ArrayList<>();

        for (JsonNode fileNode : fileList) {
            if (fileNode.path("isdir").asText().equals("1")) {
                // It's a folder - process recursively
                List<FileItem> folderFiles = processFolder(fileNode, shorturl, uk, shareId, jsToken, logid, defaultThumbnail, cookie);
                processedFiles.addAll(folderFiles);
            } else {
                // It's a file - process it
                FileItem fileItem = processFile(fileNode, uk, shareId, jsToken, defaultThumbnail, cookie);
                if (fileItem != null) {
                    processedFiles.add(fileItem);
                }
            }
        }

        return processedFiles;
    }

    /**
     * Process a single file
     */
    private FileItem processFile(JsonNode fileNode, long uk, long shareId, String jsToken, String defaultThumbnail, String cookie) {
        try {
            String dlink = fileNode.path("dlink").asText("");
            if (dlink.isEmpty()) {
                log.warn("File {} has no dlink, skipping", fileNode.path("server_filename").asText());
                return null;
            }

            // Get direct link using HEAD request
            String directLink = getDirectLink(dlink, cookie);

            // Construct stream URL
            String streamUrl = constructStreamUrl(directLink, uk, shareId, jsToken);

            // Build thumbs - extract from API response
            JsonNode thumbsNode = fileNode.path("thumbs");
            Thumbs thumbs = Thumbs.builder()
                    .url1(thumbsNode.path("url1").asText(defaultThumbnail))
                    .url2(thumbsNode.path("url2").asText(defaultThumbnail))
                    .url3(thumbsNode.path("url3").asText(defaultThumbnail))
                    .icon(thumbsNode.path("icon").asText(defaultThumbnail))
                    .build();

            // Build file item - extract all values from TeraBox API response
            return FileItem.builder()
                    .category(fileNode.path("category").asText("1"))
                    .fsId(fileNode.path("fs_id").asText(""))
                    .isdir(fileNode.path("isdir").asText("0"))
                    .localCtime(fileNode.path("local_ctime").asText(""))
                    .localMtime(fileNode.path("local_mtime").asText(""))
                    .md5(fileNode.path("md5").asText(""))
                    .path(fileNode.path("path").asText(""))
                    .playForbid(fileNode.path("play_forbid").asText("0"))
                    .serverCtime(fileNode.path("server_ctime").asText(""))
                    .serverFilename(fileNode.path("server_filename").asText(""))
                    .serverMtime(fileNode.path("server_mtime").asText(""))
                    .size(fileNode.path("size").asText(""))
                    .thumbs(thumbs)
                    .emd5(fileNode.path("emd5").asText(""))
                    .dlink(dlink)
                    .directLink(directLink)
                    .streamUrl(streamUrl)
                    .build();

        } catch (Exception e) {
            log.error("Error processing file: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Process a folder recursively with parallel processing of contents
     */
    private List<FileItem> processFolder(JsonNode folderNode, String shorturl, long uk, long shareId,
                                         String jsToken, String logid, String defaultThumbnail, String cookie) {
        try {
            String folderPath = folderNode.path("path").asText("");
            log.info("Processing folder: {}", folderPath);

            // Get folder contents
            List<JsonNode> folderContents = getFolderContents(shorturl, folderPath, jsToken, logid, cookie);

            // Convert to JsonNode array for parallel processing
            JsonNode contentsArray = objectMapper.createArrayNode();
            folderContents.forEach(((com.fasterxml.jackson.databind.node.ArrayNode) contentsArray)::add);

            // Process folder contents in parallel (same as root level)
            return processFilesAndFoldersParallel(contentsArray, shorturl, uk, shareId, jsToken, logid, defaultThumbnail, cookie);

        } catch (Exception e) {
            log.error("Error processing folder: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get contents of a folder
     */
    private List<JsonNode> getFolderContents(String shorturl, String dirPath, String jsToken, String logid, String cookie) throws Exception {
        String encodedPath = URLEncoder.encode(dirPath, StandardCharsets.UTF_8);

        String apiUrl = String.format(
                "https://dm.1024tera.com/share/list?app_id=250528&web=1&channel=dubox&clienttype=0&jsToken=%s&dp-logid=%s&by=name&order=asc&site_referer=https%%3A%%2F%%2Fwww.1024tera.com%%2F&shorturl=%s&dir=%s&root=0",
                jsToken, logid != null ? logid : "", shorturl, encodedPath
        );

        Request request = new Request.Builder()
                .url(apiUrl)
                .header("Cookie", cookie)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .header("Accept", "application/json, text/plain, */*")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to fetch folder contents: HTTP " + response.code());
            }

            String responseBody = response.body().string();
            JsonNode apiResponse = objectMapper.readTree(responseBody);

            int errno = apiResponse.path("errno").asInt(-1);
            if (errno != 0) {
                throw new Exception("API returned error for folder: errno=" + errno);
            }

            JsonNode listNode = apiResponse.path("list");
            List<JsonNode> items = new ArrayList<>();
            listNode.forEach(items::add);

            return items;
        }
    }

    /**
     * Get direct download link using HEAD request
     */
    private String getDirectLink(String dlink, String cookie) {
        try {
            Request request = new Request.Builder()
                    .url(dlink)
                    .head()
                    .header("Cookie", cookie)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .build();

            // Don't follow redirects for HEAD request
            OkHttpClient noRedirectClient = httpClient.newBuilder()
                    .followRedirects(false)
                    .build();

            try (Response response = noRedirectClient.newCall(request).execute()) {
                String location = response.header("Location");
                if (location != null && !location.isEmpty()) {
                    log.debug("Got direct link from Location header");
                    return location;
                }
            }
        } catch (Exception e) {
            log.warn("Error getting direct link: {}", e.getMessage());
        }

        return dlink; // Return original dlink if we can't get redirect
    }

    /**
     * Construct stream URL for video playback
     */
    private String constructStreamUrl(String directLink, long uk, long shareId, String jsToken) {
        try {
            // Extract parameters from direct link
            String fid = StringUtils.extractUrlParameter(directLink, "fid");
            String sign = StringUtils.extractUrlParameter(directLink, "sign");
            String timestamp = StringUtils.extractUrlParameter(directLink, "time");

            // Extract fid number (after last dash)
            if (fid != null && fid.contains("-")) {
                String[] parts = fid.split("-");
                fid = parts[parts.length - 1];
            }

            // Extract sign value (after first dash)
            if (sign != null && sign.contains("-")) {
                String[] parts = sign.split("-", 2);
                if (parts.length > 1) {
                    sign = parts[1];
                }
            }

            return String.format(
                    "https://dm.1024tera.com/share/streaming?uk=%d&shareid=%d&type=M3U8_AUTO_1080&fid=%s&sign=%s&timestamp=%s&jsToken=%s&esl=1&isplayer=1&ehps=1&clienttype=0&app_id=250528&web=1&channel=dubox",
                    uk, shareId, fid != null ? fid : "", sign != null ? sign : "", timestamp != null ? timestamp : "", jsToken
            );

        } catch (Exception e) {
            log.error("Error constructing stream URL: {}", e.getMessage());
            return "";
        }
    }
}

