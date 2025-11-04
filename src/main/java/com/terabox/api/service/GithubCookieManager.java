package com.terabox.api.service;

import jakarta.annotation.PostConstruct;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class GithubCookieManager {
    private static final Logger logger = LoggerFactory.getLogger(GithubCookieManager.class);

    @Value("${github.repo.owner}")
    private String repoOwner;

    @Value("${github.repo.name}")
    private String repoName;

    @Value("${github.token:}")
    private String token;

    private final List<String> cookies = new CopyOnWriteArrayList<>();
    private final AtomicInteger currentCookieIndex = new AtomicInteger(0);

    private final List<String> premiumCookies = new CopyOnWriteArrayList<>();
    private final AtomicInteger currentPremiumCookieIndex = new AtomicInteger(0);

    private final List<String> clientCookies = new CopyOnWriteArrayList<>();
    private final AtomicInteger currentClientCookieIndex = new AtomicInteger(0);

    private final Map<String, Long> lastFetchTimes = new ConcurrentHashMap<>();
    private final long fetchInterval = 3600000; // 1 hour in milliseconds

    private final OkHttpClient client = new OkHttpClient();

    @PostConstruct
    public void initialize() {
        cookies.addAll(fetchCookiesFromPath("cookies.txt"));
        premiumCookies.addAll(fetchCookiesFromPath("cookiesPremium.txt"));
        clientCookies.addAll(fetchCookiesFromPath("clientCookies.txt"));
        logger.info("Initialized {} regular and {} premium cookies", cookies.size(), premiumCookies.size());
    }

    private List<String> fetchCookiesFromPath(String filePath) {
        List<String> result = new ArrayList<>();
        String rawUrl = String.format("https://raw.githubusercontent.com/%s/%s/main/%s",
                repoOwner, repoName, filePath);
        try {
            logger.info("Fetching cookies from GitHub: {}", rawUrl);

            Request.Builder requestBuilder = new Request.Builder()
                    .url(rawUrl)
                    .addHeader("Accept", "application/vnd.github.v3.raw")
                    .addHeader("User-Agent", "TeraBox-Cookie-Fetcher");
            if (token != null && !token.isEmpty()) {
                requestBuilder.addHeader("Authorization", "token " + token);
            }

            try (Response response = client.newCall(requestBuilder.build()).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String content = response.body().string().trim();
                    String[] lines = content.split("\n");

                    for (String line : lines) {
                        line = line.trim();
                        if (line.contains("ndus=") && !result.contains(line)) {
                            result.add(line);
                        }
                    }
                    if (!result.isEmpty()) {
                        logger.info("Successfully fetched {} cookies from {}", result.size(), filePath);
                        lastFetchTimes.put(filePath, System.currentTimeMillis());
                    } else {
                        logger.warn("No valid cookies found in {}", filePath);
                    }
                } else {
                    logger.error("Failed to fetch {}. Status code: {}", filePath, response.code());
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching {} from GitHub: {}", filePath, e.getMessage());
        }
        return result;
    }

    public String getCookie() {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastFetchTimes.getOrDefault("cookies.txt", 0L) > fetchInterval) {
            List<String> githubCookies = fetchCookiesFromPath("cookies.txt");
            int newCookieAdded = 0;

            for (String githubCookie : githubCookies) {
                if (!cookies.contains(githubCookie)) {
                    cookies.add(githubCookie);
                    newCookieAdded++;
                }
            }
            if (newCookieAdded > 0) {
                logger.info("Added {} new regular cookies, total: {}", newCookieAdded, cookies.size());
            }
        }
        if (cookies.isEmpty()) {
            logger.error("No valid regular cookies available");
            return "";
        }
        int index = currentCookieIndex.getAndUpdate(i -> (i + 1) % cookies.size());
        return cookies.get(index);
    }

    public String getPremiumCookie() {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastFetchTimes.getOrDefault("premium_cookies.txt", 0L) > fetchInterval) {
            List<String> githubCookies = fetchCookiesFromPath("premium_cookies.txt");
            int newCookiesAdded = 0;

            for (String githubCookie : githubCookies) {
                if (!premiumCookies.contains(githubCookie)) {
                    premiumCookies.add(githubCookie);
                    newCookiesAdded++;
                }
            }

            if (newCookiesAdded > 0) {
                logger.info("Added {} new premium cookies, total: {}", newCookiesAdded, premiumCookies.size());
            }
        }

        if (premiumCookies.isEmpty()) {
            logger.error("No valid premium cookies available");
            return "";
        }

        int index = currentPremiumCookieIndex.getAndUpdate(i -> (i + 1) % premiumCookies.size());
        String cookie = premiumCookies.get(index);

        String maskedCookie = cookie.length() > 30 ? cookie.substring(0, 30) + "..." : cookie;
        logger.info("Using premium cookie #{}/{}: {}", index + 1, premiumCookies.size(), maskedCookie);

        return cookie;
    }

    public String getClientCookie() {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastFetchTimes.getOrDefault("client_cookies.txt", 0L) > fetchInterval) {
            List<String> githubCookies = fetchCookiesFromPath("client_cookies.txt");
            int newCookiesAdded = 0;

            for (String githubCookie : githubCookies) {
                if (!clientCookies.contains(githubCookie)) {
                    clientCookies.add(githubCookie);
                    newCookiesAdded++;
                }
            }

            if (newCookiesAdded > 0) {
                logger.info("Added {} new client cookies, total: {}", newCookiesAdded, clientCookies.size());
            }
        }

        if (clientCookies.isEmpty()) {
            logger.error("No valid client cookies available");
            return "";
        }

        int index = currentClientCookieIndex.getAndUpdate(i -> (i + 1) % clientCookies.size());
        String cookie = clientCookies.get(index);

        String maskedCookie = cookie.length() > 30 ? cookie.substring(0, 30) + "..." : cookie;
        logger.info("Using client cookie #{}/{}: {}", index + 1, clientCookies.size(), maskedCookie);

        return cookie;
    }

    public void forceRefresh() {
        logger.info("Forcing refresh of all cookies from GitHub");
        cookies.clear();
        premiumCookies.clear();
        clientCookies.clear();
        initialize();
    }
}
