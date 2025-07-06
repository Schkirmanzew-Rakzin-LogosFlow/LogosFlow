package io.logosflow.modules;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author Shkirmantsev
 */
@EnableDiscoveryClient
@SpringBootApplication
public class LogosFlowGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(LogosFlowGatewayApplication.class, args);
    }
}
