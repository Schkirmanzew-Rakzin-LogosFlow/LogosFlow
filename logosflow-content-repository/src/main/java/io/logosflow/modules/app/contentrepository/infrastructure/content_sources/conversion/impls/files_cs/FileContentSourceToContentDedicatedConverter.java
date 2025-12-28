package io.logosflow.modules.app.contentrepository.infrastructure.content_sources.conversion.impls.files_cs;

import io.logosflow.modules.app.contentrepository.infrastructure.content_sources.ContentSource;
import io.logosflow.modules.app.contentrepository.infrastructure.content_sources.conversion.ContentSourceToContentDedicatedConverter;
import io.logosflow.modules.app.contentrepository.infrastructure.content_sources.files.MultipartContentSource;
import io.logosflow.modules.app.contentrepository.models.content.Content;
import io.logosflow.modules.app.contentrepository.models.content.impls.DefaultContent;
import io.logosflow.modules.app.contentrepository.resources.resolvers.uris.UriResolverFacade;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * This adapter is responsible for converting {@link MultipartFile} to {@link Content}
 *
 * @author Shkirmantsev
 */
@Component
@RequiredArgsConstructor
public class FileContentSourceToContentDedicatedConverter implements ContentSourceToContentDedicatedConverter {

    private final UriResolverFacade uriResolverFacade;

    @Override
    public boolean isSuitable(ContentSource contentSource) {
        return contentSource.getContentSourceType().equals(MultipartContentSource.CONTENT_SOURCE_TYPE);
    }

    @Override
    public Content contentFrom(ContentSource contentSource) {
        if (!isSuitable(contentSource)) {
            throw throwIllegalArgumentException(contentSource);
        }

        if (contentSource instanceof MultipartContentSource mcr) {
            return Optional.of(mcr)// always not null
                    .map(MultipartContentSource::getFile)
                    .map(MultipartFile::getContentType)
                    .map(contentType ->
                            DefaultContent.builder()
                                    .mediaType(MediaType.valueOf(contentType))
                                    .dataSupplier(inputStreamFrom(mcr))
                                    .metaInfo(mcr.getMetaInfo())
                                    .id(() -> uriResolverFacade.resolve(mcr.getFile()))
                                    .dataSizeInBytesOrNull(mcr.getFile().getSize())
                                    .build()
                    )
                    .orElseThrow(() -> throwIllegalArgumentException(contentSource));
        } else {
            throw throwIllegalArgumentException(contentSource);

        }
    }

    private static Supplier<InputStream> inputStreamFrom(MultipartContentSource multipartContentSource) {
        return () -> {
            try {
                return multipartContentSource.getFile().getInputStream();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    private static @NonNull IllegalArgumentException throwIllegalArgumentException(ContentSource contentSource) {
        return new IllegalArgumentException(
                String.format("Content source %s is not suitable for this adapter %s",
                        contentSource,
                        FileContentSourceToContentDedicatedConverter.class.getSimpleName()));
    }
}
