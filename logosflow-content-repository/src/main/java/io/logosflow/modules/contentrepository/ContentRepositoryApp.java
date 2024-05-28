package io.logosflow.modules.contentrepository;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author Shkirmantsev
 */
@EnableDiscoveryClient
@SpringBootApplication
public class ContentRepositoryApp {

    public static void main(String[] args) {
        SpringApplication.run(ContentRepositoryApp.class, args);
    }

}
