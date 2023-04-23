package io.logosflow.modules;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Hello world!
 *
 */
@EnableDiscoveryClient
@SpringBootApplication
public class LibrariesServer
{
    public static void main( String[] args )
    {
        SpringApplication.run(LibrariesServer.class, args);
    }
}
