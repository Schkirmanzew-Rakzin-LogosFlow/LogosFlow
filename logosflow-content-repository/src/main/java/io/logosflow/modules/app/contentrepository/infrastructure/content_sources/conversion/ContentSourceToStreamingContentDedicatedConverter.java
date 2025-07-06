package io.logosflow.modules.app.contentrepository.infrastructure.content_sources.conversion;

import io.logosflow.modules.app.contentrepository.infrastructure.content_sources.ContentSource;
import io.logosflow.modules.app.contentrepository.models.content.StreamingContent;

/**
 * This interface should be implemented by the adapter that is responsible for converting the specific <br>
 * content source to the content. It should be used by general {@link ContentSourceToContentConversionFacade}.
 *
 * @author Shkirmantsev
 * @see ContentSourceToContentConversionFacade
 */
public interface ContentSourceToStreamingContentDedicatedConverter {

    boolean isSuitable(ContentSource contentSource);

    StreamingContent contentFrom(ContentSource contentSource);
}
