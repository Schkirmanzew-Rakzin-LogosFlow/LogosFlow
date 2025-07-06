package io.logosflow.modules.app.contentrepository.models.content.impls;

import io.logosflow.modules.app.contentrepository.models.content.ContentId;
import io.logosflow.modules.app.contentrepository.models.content.StreamingContent;
import lombok.Builder;
import lombok.Getter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author Shkirmantsev
 */
@Builder
@Getter
public class FluxStreamingContent implements StreamingContent {

    private final MediaType mediaType;

    private final Supplier<Flux<DataBuffer>> dataSupplier;

    private final Map<String, String> metaInfo;

    private final ContentId id;

    private final Long dataSizeInBytesOrNull;


    @Override
    public Optional<Long> getDataSizeInBytes() {
        return Optional.empty();
    }
}
