package io.logosflow.modules.app.contentrepository.infrastructure.controllers;

import io.logosflow.modules.app.contentrepository.infrastructure.content_sources.conversion.ContentSourceToStreamingContentConversionFacade;
import io.logosflow.modules.app.contentrepository.infrastructure.content_sources.files.FilePartContentSource;
import io.logosflow.modules.app.contentrepository.infrastructure.content_sources.files.ReactiveStreamContentSource;
import io.logosflow.modules.app.contentrepository.models.content.StreamingContent;
import io.logosflow.modules.app.contentrepository.resources.AllContentsRepositoryFacade;
import io.logosflow.modules.app.contentrepository.resources.ResourceSimulationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static io.logosflow.modules.app.contentrepository.infrastructure.controllers.ContentResourceRestReactiveControllerV1.BASE_URL;
import static java.util.Collections.emptyMap;

@RestController
@RequestMapping(BASE_URL)
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class ContentResourceRestReactiveControllerV1 {

    public static final String API_VERSION_PREFIX = "/api/v1/reactive/content";

    public static final String BASE_URL = API_VERSION_PREFIX + "/repositories/content/resources";

    private final ResourceSimulationRepository resourceSimulationRepository;
    private final ContentSourceToStreamingContentConversionFacade contentAdapter;
    private final AllContentsRepositoryFacade allContentsRepositoryFacade;

    @PostMapping(value = "/timed-resource")
    public CompletableFuture<String> ping(@RequestParam String requestId, @RequestParam Long timing) {
        var resource = resourceSimulationRepository.getResourceByTiming(requestId, timing);
        log.info(resource);

        return CompletableFuture.completedFuture(resource);
    }

    @PostMapping(value = "/file/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public Mono<ResponseEntity<String>> uploadFile(@RequestPart("file") Mono<FilePart> filePartMono) {

        return filePartMono.flatMap(file -> {
            log.info("Uploading file: {}", file.filename());

            var contentSource = FilePartContentSource.builder(file)
                    .fileName(file.filename())
                    .fileVersion(UUID.randomUUID().toString())
                    .metaInfo(emptyMap())
                    .build();

            var content = contentAdapter.contentFrom(contentSource);

            return allContentsRepositoryFacade
                    .save(content)
                    .map(resourceId -> ResponseEntity
                            .status(HttpStatus.CREATED)
                            .contentType(MediaType.TEXT_PLAIN)
                            .body(resourceId))
                    .onErrorResume(e -> {
                        log.error("Error during file upload: {}", e.getMessage(), e);

                        return Mono.just(ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage()));
                    });
        });
    }

    @PostMapping(value = "/upload/byte-stream/{resourceName}", produces = MediaType.TEXT_PLAIN_VALUE)
    public Mono<ResponseEntity<String>> uploadByteStream(
            @PathVariable("resourceName") String resourceName,
            @RequestBody Flux<DataBuffer> dataBuffer) {

        var contentSource = ReactiveStreamContentSource
                .builder(dataBuffer)
                .resourceName(resourceName)
                .resourceVersion(UUID.randomUUID().toString())
                .metaInfo(emptyMap())
                .build();

        StreamingContent content = contentAdapter.contentFrom(contentSource);

        allContentsRepositoryFacade
                .save(content)
                .map(resourceId -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .contentType(MediaType.TEXT_PLAIN)
                        .body(resourceId))
                .onErrorResume(e -> {
                    log.error("Error during byte-stream upload: {}", e.getMessage(), e);

                    return Mono.just(ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage()));
                });

        return Mono.fromRunnable(() -> ResponseEntity
                .status(HttpStatus.CREATED)
                .contentType(MediaType.TEXT_PLAIN)
                .body(allContentsRepositoryFacade.save(content)
                ));
    }

    @GetMapping("/resource/is-exist/{resourceId}")
    public Mono<Boolean> isExist(@PathVariable(value = "resourceId") UUID resourceId) {
        return allContentsRepositoryFacade.checkExistingContentAsynchronously(resourceId).hasElement();
    }
}
