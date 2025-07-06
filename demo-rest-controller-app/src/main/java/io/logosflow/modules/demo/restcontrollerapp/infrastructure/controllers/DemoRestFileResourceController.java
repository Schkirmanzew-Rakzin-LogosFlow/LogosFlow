package io.logosflow.modules.demo.restcontrollerapp.infrastructure.controllers;

import io.logosflow.modules.demo.restcontrollerapp.resources.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

import static io.logosflow.modules.demo.restcontrollerapp.infrastructure.controllers.DemoRestFileResourceController.BASE_URL;
import static io.logosflow.modules.demo.restcontrollerapp.threads.executors.AppThreadExecutorsConfiguration.APP_CASHED_POOL_EXECUTOR;

@RestController
@RequestMapping(BASE_URL)
@RequiredArgsConstructor
public class DemoRestFileResourceController {

    public static final String BASE_URL = "demo/controller/rest/resources";

    private final ResourceRepository resourceRepository;

    @Async(APP_CASHED_POOL_EXECUTOR)
    @PostMapping(value = "/timed-resource")
    public CompletableFuture<String> ping(@RequestParam String requestId, @RequestParam Long timing) {
        var resource = resourceRepository.getResourceByTiming(requestId, timing);
        System.out.println(resource);
        return CompletableFuture.completedFuture(resource);
    }
}
