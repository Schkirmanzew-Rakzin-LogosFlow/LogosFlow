package io.logosflow.modules.contentrepository.infrastructure.controllers;

import io.logosflow.modules.contentrepository.resources.ResourceRepository;
import io.logosflow.modules.contentrepository.threads.executors.AppThreadExecutorsConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

import static io.logosflow.modules.contentrepository.infrastructure.controllers.RestContentResourceDefaultController.BASE_URL;

@RestController
@RequestMapping(BASE_URL)
@RequiredArgsConstructor
public class RestContentResourceDefaultController {

    public static final String API_VERSION_PREFIX = "/api/v1";

    public static final String BASE_URL = API_VERSION_PREFIX + "/repositories/content/resources";

    private final ResourceRepository resourceRepository;

    //TODO fix path
    @Async(AppThreadExecutorsConfiguration.APP_CASHED_POOL_EXECUTOR)
    @PostMapping(value = "/timed-resource")
    public CompletableFuture<String> ping(@RequestParam String requestId, @RequestParam Long timing) {
        var resource = resourceRepository.getResourceByTiming(requestId, timing);
        System.out.println(resource);
        return CompletableFuture.completedFuture(resource);
    }
}
