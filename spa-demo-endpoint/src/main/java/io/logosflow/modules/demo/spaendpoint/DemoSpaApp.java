package io.logosflow.modules.demo.spaendpoint;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author Shkirmantsev
 */
@EnableDiscoveryClient
@SpringBootApplication
public class DemoSpaApp {

    public static void main(String[] args) {
        SpringApplication.run(DemoSpaApp.class, args);
    }
}
