package io.logosflow.modules.app.contentrepository.infrastructure.content_sources.conversion;

import io.logosflow.modules.app.contentrepository.infrastructure.content_sources.ContentSource;
import io.logosflow.modules.app.contentrepository.models.content.Content;
import org.springframework.stereotype.Service;

/**
 * This interface should be implemented by the adapter that is responsible for converting the specific <br>
 * content source to the content. It should be used by general {@link ContentSourceToContentConversionFacade}.
 *
 * @author Shkirmantsev
 * @see ContentSourceToContentConversionFacade
 */
@Service
public interface ContentSourceToContentDedicatedConverter {

    boolean isSuitable(ContentSource contentSource);

    Content contentFrom(ContentSource contentSource);
}
