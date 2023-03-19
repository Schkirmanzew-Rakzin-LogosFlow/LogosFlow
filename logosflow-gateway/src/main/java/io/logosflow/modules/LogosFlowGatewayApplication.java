package io.logosflow.modules;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * @author Shkirmantsev
 */
@EnableEurekaClient
@SpringBootApplication
public class LogosFlowGatewayApplication
{
    public static void main( String[] args )
    {
        SpringApplication.run(LogosFlowGatewayApplication.class, args);
    }
}
