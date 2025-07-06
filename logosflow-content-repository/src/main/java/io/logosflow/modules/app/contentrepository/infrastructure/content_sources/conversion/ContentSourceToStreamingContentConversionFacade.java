package io.logosflow.modules.app.contentrepository.infrastructure.content_sources.conversion;

import io.logosflow.modules.app.contentrepository.infrastructure.content_sources.ContentSource;
import io.logosflow.modules.app.contentrepository.models.content.StreamingContent;

/**
 * @author Shkirmantsev
 */
public interface ContentSourceToStreamingContentConversionFacade {

    StreamingContent contentFrom(ContentSource contentSource);

    class ReactiveContentAdapterException extends RuntimeException {

        public ReactiveContentAdapterException(String message) {
            super(message);
        }

        public ReactiveContentAdapterException(Throwable cause) {
            super(cause);
        }

        public ReactiveContentAdapterException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
