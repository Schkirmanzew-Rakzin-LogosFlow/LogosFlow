package io.logosflow.modules.app.contentrepository.infrastructure.content_sources.conversion.impls.files_cs;

import io.logosflow.modules.app.contentrepository.infrastructure.content_sources.ContentSource;
import io.logosflow.modules.app.contentrepository.infrastructure.content_sources.conversion.ContentSourceToStreamingContentDedicatedConverter;
import io.logosflow.modules.app.contentrepository.infrastructure.content_sources.files.MultipartContentSource;
import io.logosflow.modules.app.contentrepository.models.content.StreamingContent;
import io.logosflow.modules.app.contentrepository.models.content.impls.FluxStreamingContent;
import io.logosflow.modules.app.contentrepository.resources.resolvers.uris.UriResolverFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;

/**
 * @author Shkirmantsev
 */
@Component
@RequiredArgsConstructor
public class FileContentSourceToStreamingContentDedicatedConverter implements ContentSourceToStreamingContentDedicatedConverter {

    private final UriResolverFacade uriResolverFacade;

    @Override
    public boolean isSuitable(ContentSource contentSource) {
        return contentSource.getContentSourceType().equals(MultipartContentSource.CONTENT_SOURCE_TYPE);
    }

    @Override
    public StreamingContent contentFrom(ContentSource contentSource) {
        if (!isSuitable(contentSource)) {
            throw new IllegalArgumentException("Unsuitable content source: " + contentSource);
        }

        if (contentSource instanceof MultipartContentSource mcr) {
            return FluxStreamingContent.builder()
                    .mediaType(MediaType.valueOf(
                            Optional.ofNullable(mcr.getFile().getContentType())
                                    .orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE)))
                    .dataSupplier(() -> DataBufferUtils.readInputStream(
                            () -> {
                                try {
                                    return mcr.getFile().getInputStream();
                                } catch (IOException e) {
                                    throw new UncheckedIOException(e);
                                }
                            },
                            new DefaultDataBufferFactory(),
                            4096))
                    .metaInfo(mcr.getMetaInfo())
                    .id(() -> uriResolverFacade.resolve(mcr.getFile()))
                    .dataSizeInBytesOrNull(mcr.getFile().getSize())
                    .build();
        }

        throw new IllegalArgumentException("Unsupported content source type");
    }
}
