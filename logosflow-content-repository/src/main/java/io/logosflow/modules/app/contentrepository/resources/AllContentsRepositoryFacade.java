package io.logosflow.modules.app.contentrepository.resources;

import io.logosflow.modules.app.contentrepository.models.content.Content;
import io.logosflow.modules.app.contentrepository.models.content.ContentId;
import io.logosflow.modules.app.contentrepository.models.content.StreamingContent;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

/**
 * @author Shkirmantsev
 */
public interface AllContentsRepositoryFacade {

    String save(Content content);

    Optional<Content> find(ContentId contentId);

    /**
     * @param resourceId resource id for finding content in the content repository
     * @return Optional<String> representation of the resource info
     */
    Optional<Content> find(UUID resourceId);

    /**
     * @param resourceId resource id for finding content in the content repository
     * @return Optional<String> representation of the resource info
     */
    Optional<String> checkExistingContent(UUID resourceId);

    Mono<String> save(StreamingContent content);

    Mono<String> checkExistingContentAsynchronously(UUID resourceId);

    Mono<StreamingContent> findAsynchronously(ContentId contentId);

    Mono<StreamingContent> findAsynchronously(UUID resourceId);

    class ContentGeneralRepositoryException extends RuntimeException {

        public ContentGeneralRepositoryException(String message) {
            super(message);
        }

        public ContentGeneralRepositoryException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
