package io.logosflow.modules.app.contentrepository.resources;

import io.logosflow.modules.app.contentrepository.models.content.Content;
import io.logosflow.modules.app.contentrepository.models.content.ContentId;
import io.logosflow.modules.app.contentrepository.models.content.StreamingContent;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

/**
 * Each implementation of this interface should be responsible for saving the specific content type.
 *
 * @author Shkirmantsev
 */
public interface ContentSpecificDedicatedRepository {

    String save(Content content);

    Optional<Content> find(ContentId contentId);

    Optional<Content> find(UUID resourceId);

    /**
     * @param resourceId resource id for finding content in the content repository
     * @return Optional<String> representation of the resource info
     */
    Optional<String> checkExistingContent(UUID resourceId);

    Mono<String> save(StreamingContent content);

    default Mono<String> checkExistingContentAsynchronously(UUID resourceId) {
        return Mono.defer(() -> {
            String resourceInfo = checkExistingContent(resourceId).orElse(null);
            return resourceInfo == null ? Mono.empty() : Mono.just(resourceInfo);
        });
    }

    Mono<StreamingContent> findAsynchronously(ContentId contentId);

    Mono<StreamingContent> findAsynchronously(UUID resourceId);

    /**
     * Either content could be saved by this repository
     */
    boolean isSuitable(Content content);

    /**
     * Either content could be saved by this repository
     */
    boolean isSuitable(StreamingContent content);

    boolean isSuitable(ContentId contentId);

    class ContentSpecificRepositoryException extends RuntimeException {

        public ContentSpecificRepositoryException(String message) {
            super(message);
        }

        public ContentSpecificRepositoryException(Throwable cause) {
            super(cause);
        }

        public ContentSpecificRepositoryException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
