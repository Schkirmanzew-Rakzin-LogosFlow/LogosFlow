package io.logosflow.modules.app.contentrepository.infrastructure.content_sources.conversion.impls;

import io.logosflow.modules.app.contentrepository.infrastructure.content_sources.ContentSource;
import io.logosflow.modules.app.contentrepository.infrastructure.content_sources.conversion.ContentSourceToContentConversionFacade;
import io.logosflow.modules.app.contentrepository.infrastructure.content_sources.conversion.ContentSourceToContentDedicatedConverter;
import io.logosflow.modules.app.contentrepository.models.content.Content;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Shkirmantsev
 */
@Service
@RequiredArgsConstructor
public class ContentSourceToContentConversionFacadeImpl implements ContentSourceToContentConversionFacade {

    private final List<ContentSourceToContentDedicatedConverter> contentSourceToContentDedicatedConverters;

    @Override
    public Content contentFrom(ContentSource contentSource) {
        return contentSourceToContentDedicatedConverters
                .stream()
                .filter(adapter -> adapter.isSuitable(contentSource))
                .findFirst()
                .map(adapter -> adapter.contentFrom(contentSource))
                .orElseThrow(() ->
                        new ContentAdapterException("No suitable adapter found for content source: " + contentSource));
    }
}
