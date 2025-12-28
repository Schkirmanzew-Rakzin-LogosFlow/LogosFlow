package io.logosflow.modules.app.contentrepository.infrastructure.content_sources.files;

import io.logosflow.modules.app.contentrepository.infrastructure.content_sources.ContentSource;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * @author Shkirmantsev
 */
@Getter
@Builder(builderMethodName = "hiddenBuilder")
public class ReactiveStreamContentSource implements ContentSource {

    public static final String CONTENT_SOURCE_TYPE = "reactiveByteStream";

    @NonNull
    private final Flux<DataBuffer> dataBuffer;

    private String resourceName;

    private String resourceVersion;

    private Map<String, String> metaInfo;

    @Override
    public String getContentSourceType() {
        return CONTENT_SOURCE_TYPE;
    }

    public static ReactiveStreamContentSource.ReactiveStreamContentSourceBuilder builder(
            @NonNull Flux<DataBuffer> dataBuffer) {
        return hiddenBuilder().dataBuffer(dataBuffer);
    }
}
