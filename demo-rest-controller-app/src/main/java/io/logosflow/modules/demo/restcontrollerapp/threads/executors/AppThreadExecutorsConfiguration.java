package io.logosflow.modules.demo.restcontrollerapp.threads.executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Shkirmantsev
 */
@Configuration
@EnableAsync
public class AppThreadExecutorsConfiguration {

    public static final String APP_CASHED_POOL_EXECUTOR = "appCachedThreadPoolExecutor";

    @Bean(APP_CASHED_POOL_EXECUTOR)
    public ExecutorService cachedPoolExecutor() {
        return Executors.newCachedThreadPool();
    }
}
