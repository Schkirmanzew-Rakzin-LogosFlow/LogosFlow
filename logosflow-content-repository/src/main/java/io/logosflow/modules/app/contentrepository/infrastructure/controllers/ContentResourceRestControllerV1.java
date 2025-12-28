package io.logosflow.modules.app.contentrepository.infrastructure.controllers;

import io.logosflow.modules.app.contentrepository.infrastructure.content_sources.conversion.ContentSourceToContentConversionFacade;
import io.logosflow.modules.app.contentrepository.infrastructure.content_sources.files.MultipartContentSource;
import io.logosflow.modules.app.contentrepository.models.content.Content;
import io.logosflow.modules.app.contentrepository.resources.AllContentsRepositoryFacade;
import io.logosflow.modules.app.contentrepository.resources.ResourceSimulationRepository;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

import static io.logosflow.modules.app.contentrepository.infrastructure.controllers.ContentResourceRestControllerV1.BASE_URL;
import static java.util.Collections.emptyMap;

@RestController
@RequestMapping(BASE_URL)
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class ContentResourceRestControllerV1 {

    public static final String API_VERSION_PREFIX = "/api/v1/synchrony/content";
    public static final String BASE_URL = API_VERSION_PREFIX + "/repositories/content/resources";

    private final ResourceSimulationRepository resourceSimulationRepository;
    private final ContentSourceToContentConversionFacade contentSourceToContentConversionFacade;
    private final AllContentsRepositoryFacade allContentsRepositoryFacade;

    /**
     * Endpoint to simulate retrieving a resource based on timing.
     */
    @PostMapping(value = "/timed-resource")
    public String ping(@RequestParam String requestId, @RequestParam Long timing) {
        var resource = resourceSimulationRepository.getResourceByTiming(requestId, timing);
        log.info(resource);
        return resource;
    }

    /**
     * Endpoint to upload a file to the content repository.
     */
    @PostMapping(value = "/file/upload", produces = {MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<String> uploadFile(@RequestPart("file") MultipartFile file) {
        if (file == null) {
            log.error("File is null");
            return ResponseEntity.badRequest().body("File is null");
        }

        var contentSource = MultipartContentSource.builder(file)
                .fileName(Optional.ofNullable(file.getOriginalFilename()).orElse(file.getName()))
                .fileVersion(UUID.randomUUID().toString())
                .metaInfo(emptyMap())
                .build();

        try {
            Content content = contentSourceToContentConversionFacade.contentFrom(contentSource);
            return saveContentIntoContentRepository(content);
        } catch (Exception e) {
            log.error("File upload failed", e);
            return ResponseEntity.internalServerError().body("File upload failed");
        }
    }

    private @NonNull ResponseEntity<String> saveContentIntoContentRepository(Content content) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .contentType(MediaType.TEXT_PLAIN)
                .body(allContentsRepositoryFacade.save(content));
    }

    /**
     * Endpoint to retrieve content by resource ID.
     */
    @GetMapping("/resource/{resourceId}")
    public ResponseEntity<Resource> getContent(@PathVariable UUID resourceId) {
        log.debug("Retrieving content for resourceId: {}", resourceId);

        return allContentsRepositoryFacade.find(resourceId)
                .map(content -> {
                    try {
                        InputStreamResource resource = new InputStreamResource(content.getDataSupplier().get());
                        HttpHeaders headers = buildHeaders(content);

                        return ResponseEntity.ok()
                                .headers(headers)
                                .contentType(content.getMediaType())
                                .body((Resource) resource);
                    } catch (Exception e) {
                        log.error("Error retrieving content for resourceId: {}", resourceId, e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).<Resource>build();
                    }
                })
                .orElseGet(() -> {
                    log.debug("Content not found for resourceId: {}", resourceId);
                    return ResponseEntity.notFound().build();
                });
    }

    private HttpHeaders buildHeaders(Content content) {
        HttpHeaders headers = new HttpHeaders();
        content.getDataSizeInBytes().ifPresent(headers::setContentLength);

        // Add cache control headers
        headers.setCacheControl("public, max-age=3600");

        // Add content disposition for file downloads
        String filename = extractFilenameFromMetadata(content);
        if (filename != null) {
            headers.setContentDispositionFormData("attachment", filename);
        }

        return headers;
    }

    private String extractFilenameFromMetadata(Content content) {
        return content.getMetaInfo().get("filename");
    }

    /**
     * Endpoint to get content metadata by resource ID.
     */
    @GetMapping("/resource/{resourceId}/metadata")
    public ResponseEntity<ContentMetadataResponse> getContentMetadata(@PathVariable UUID resourceId) {
        log.debug("Retrieving metadata for resourceId: {}", resourceId);

        return allContentsRepositoryFacade.find(resourceId)
                .map(content -> {
                    ContentMetadataResponse metadata = ContentMetadataResponse.builder()
                            .resourceId(resourceId)
                            .mediaType(content.getMediaType().toString())
                            .sizeInBytes(content.getDataSizeInBytes().orElse(null))
                            .metaInfo(content.getMetaInfo())
                            .contentUri(content.getId().uri().toString())
                            .build();

                    return ResponseEntity.ok(metadata);
                })
                .orElseGet(() -> {
                    log.debug("Content metadata not found for resourceId: {}", resourceId);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * Endpoint to check if the content with the provided resource id exists in the content repository.
     */
    @GetMapping("/resource/is-exist/{resourceId}")
    public Boolean isExist(@PathVariable(value = "resourceId") UUID resourceId) {
        return allContentsRepositoryFacade.checkExistingContent(resourceId).isPresent();
    }

    /**
     * Endpoint to delete content by resource ID.
     */
    @DeleteMapping("/resource/{resourceId}")
    public ResponseEntity<String> deleteContent(@PathVariable UUID resourceId) {
        log.debug("Deleting content for resourceId: {}", resourceId);

        try {
            boolean exists = allContentsRepositoryFacade.checkExistingContent(resourceId).isPresent();
            if (!exists) {
                return ResponseEntity.notFound().build();
            }

            // TODO: Implement delete functionality in repository layer
            log.warn("Delete functionality not yet implemented for resourceId: {}", resourceId);
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                    .body("Delete functionality not yet implemented");

        } catch (Exception e) {
            log.error("Error deleting content for resourceId: {}", resourceId, e);
            return ResponseEntity.internalServerError().body("Delete operation failed");
        }
    }

    // Helper class for metadata response
    @Builder
    @Data
    public static class ContentMetadataResponse {
        private UUID resourceId;
        private String mediaType;
        private Long sizeInBytes;
        private java.util.Map<String, String> metaInfo;
        private String contentUri;
    }
}