package io.logosflow.modules.app.contentrepository.models.content.impls;

import io.logosflow.modules.app.contentrepository.models.content.Content;
import io.logosflow.modules.app.contentrepository.models.content.ContentId;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.MediaType;

import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author Shkirmantsev
 */
@Builder
@Getter
public class DefaultContent implements Content {

    private final MediaType mediaType;

    private final Supplier<InputStream> dataSupplier;

    private final Map<String, String> metaInfo;

    private final ContentId id;

    private final Long dataSizeInBytesOrNull;

    @Override
    public Optional<Long> getDataSizeInBytes() {
        return Optional.ofNullable(dataSizeInBytesOrNull);
    }
}
