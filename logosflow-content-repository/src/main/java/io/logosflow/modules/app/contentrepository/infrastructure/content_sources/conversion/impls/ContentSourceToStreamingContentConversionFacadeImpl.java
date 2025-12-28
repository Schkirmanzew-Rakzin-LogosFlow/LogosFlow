package io.logosflow.modules.app.contentrepository.infrastructure.content_sources.conversion.impls;

import io.logosflow.modules.app.contentrepository.infrastructure.content_sources.ContentSource;
import io.logosflow.modules.app.contentrepository.infrastructure.content_sources.conversion.ContentSourceToStreamingContentConversionFacade;
import io.logosflow.modules.app.contentrepository.infrastructure.content_sources.conversion.ContentSourceToStreamingContentDedicatedConverter;
import io.logosflow.modules.app.contentrepository.models.content.StreamingContent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Shkirmantsev
 */
@Service
@RequiredArgsConstructor
public class ContentSourceToStreamingContentConversionFacadeImpl
        implements ContentSourceToStreamingContentConversionFacade {

    private final List<ContentSourceToStreamingContentDedicatedConverter> contentSourceAdapters;

    @Override
    public StreamingContent contentFrom(ContentSource contentSource) {
        return contentSourceAdapters
                .stream()
                .filter(converter -> converter.isSuitable(contentSource))
                .findFirst()
                .map(converter -> converter.contentFrom(contentSource))
                .orElseThrow(() ->
                        new ReactiveContentAdapterException(
                                "No suitable adapter found for content source: " + contentSource)
                );
    }
}
