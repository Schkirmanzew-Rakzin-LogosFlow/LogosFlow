package io.logosflow.modules.demo.restcontrollerapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author Shkirmantsev
 */
@EnableDiscoveryClient
@SpringBootApplication
public class DemoRestResourceApp {

	public static void main(String[] args) {
		SpringApplication.run(DemoRestResourceApp.class, args);
	}

}
