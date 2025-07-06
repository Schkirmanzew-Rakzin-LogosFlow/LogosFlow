package io.logosflow.modules.app.contentrepository.models.content;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author Shkirmantsev
 */
public interface StreamingContent {

    MediaType getMediaType();

    Supplier<Flux<DataBuffer>> getDataSupplier() throws IOException;

    Map<String, String> getMetaInfo();

    ContentId getId();

    Optional<Long> getDataSizeInBytes();
}
