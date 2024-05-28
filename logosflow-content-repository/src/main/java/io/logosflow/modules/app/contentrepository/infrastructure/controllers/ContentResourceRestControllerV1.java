package io.logosflow.modules.app.contentrepository.infrastructure.controllers;

import io.logosflow.modules.app.contentrepository.infrastructure.content_sources.conversion.ContentSourceToContentConversionFacade;
import io.logosflow.modules.app.contentrepository.infrastructure.content_sources.files.MultipartContentSource;
import io.logosflow.modules.app.contentrepository.models.content.Content;
import io.logosflow.modules.app.contentrepository.resources.AllContentsRepositoryFacade;
import io.logosflow.modules.app.contentrepository.resources.ResourceSimulationRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
     *
     * @param requestId the unique identifier for the request
     * @param timing    the timing delay for the resource retrieval
     * @return the simulated resource details as a String
     */
    @PostMapping(value = "/timed-resource")
    public String ping(@RequestParam String requestId, @RequestParam Long timing) {
        // Retrieve the resource based on the provided requestId and timing
        var resource = resourceSimulationRepository.getResourceByTiming(requestId, timing);

        // Log the retrieved resource information
        log.info(resource);

        // Return the resource details
        return resource;
    }

    /**
     * Endpoint to upload a file to the content repository.
     *
     * @param file the file to be uploaded
     * @return a ResponseEntity containing the resource id of the uploaded content
     */
    @PostMapping(value = "/file/upload", produces = {MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<String> uploadFile(@RequestPart("file") MultipartFile file) {
        if (file == null) {
            log.error("File is null");
            return ResponseEntity.badRequest().body("File is null");
        }

        // Create a MultipartContentSource from the provided file
        var contentSource = MultipartContentSource.builder(file)
                .fileName(Optional.ofNullable(file.getOriginalFilename()).orElse(file.getName()))
                .fileVersion(UUID.randomUUID().toString())
                .metaInfo(emptyMap())
                .build();

        try {
            Content content = contentSourceToContentConversionFacade.contentFrom(contentSource);

            // Save the content to the content repository
            return saveContentIntoContentRepository(content);
        } catch (Exception e) {
            // Log the error and return an internal server error response
            log.error("File upload failed", e);
            return ResponseEntity.internalServerError().body("File upload failed");
        }
    }

    /**
     * Endpoint to check if the content with the provided resource id exists in the content repository.
     *
     * @param resourceId the resource id of the content to be checked
     * @return true if the content with the provided resource id exists, false otherwise
     */
    @GetMapping("/resource/is-exist/{resourceId}")
    public Boolean isExist(@PathVariable(value = "resourceId") UUID resourceId) {
        return allContentsRepositoryFacade.checkExistingContent(resourceId).isPresent();
    }

    private @NonNull ResponseEntity<String> saveContentIntoContentRepository(Content content) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .contentType(MediaType.TEXT_PLAIN)
                .body(allContentsRepositoryFacade.save(content));
    }
}