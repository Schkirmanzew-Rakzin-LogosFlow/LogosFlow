package io.logosflow.modules.app.contentrepository.infrastructure.content_sources.conversion;

import io.logosflow.modules.app.contentrepository.infrastructure.content_sources.ContentSource;
import io.logosflow.modules.app.contentrepository.models.content.Content;

/**
 * @author Shkirmantsev
 */
public interface ContentSourceToContentConversionFacade {

    Content contentFrom(ContentSource contentSource);

    class ContentAdapterException extends RuntimeException {
        public ContentAdapterException(String message) {
            super(message);
        }

        public ContentAdapterException(Throwable cause) {
            super(cause);
        }

        public ContentAdapterException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
