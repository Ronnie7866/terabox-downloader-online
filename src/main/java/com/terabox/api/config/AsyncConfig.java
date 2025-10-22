package com.terabox.api.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Async configuration for parallel processing of files and folders
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {
    
    private final TeraBoxProperties properties;
    
    public AsyncConfig(TeraBoxProperties properties) {
        this.properties = properties;
    }
    
    /**
     * Thread pool executor for parallel file/folder processing
     */
    @Bean(name = "teraboxTaskExecutor")
    public Executor teraboxTaskExecutor() {
        TeraBoxProperties.AsyncConfig asyncConfig = properties.getAsync();
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(asyncConfig.getCorePoolSize());
        executor.setMaxPoolSize(asyncConfig.getMaxPoolSize());
        executor.setQueueCapacity(asyncConfig.getQueueCapacity());
        executor.setThreadNamePrefix("terabox-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        
        log.info("Initialized TeraBox async executor - Core: {}, Max: {}, Queue: {}", 
                asyncConfig.getCorePoolSize(), 
                asyncConfig.getMaxPoolSize(), 
                asyncConfig.getQueueCapacity());
        
        return executor;
    }
}

