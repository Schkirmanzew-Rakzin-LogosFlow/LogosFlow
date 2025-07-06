package io.logosflow.modules.app.contentrepository.models.content;

import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author Shkirmantsev
 */
public interface Content {

    MediaType getMediaType();

    Supplier<InputStream> getDataSupplier() throws IOException;

    Map<String, String> getMetaInfo();

    ContentId getId();

    Optional<Long> getDataSizeInBytes();
}
