package io.logosflow.modules.app.contentrepository.infrastructure.content_sources.files;

import io.logosflow.modules.app.contentrepository.infrastructure.content_sources.ContentSource;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.http.codec.multipart.FilePart;

import java.util.Map;

/**
 * @author Shkirmantsev
 */
@Getter
@Builder(builderMethodName = "hiddenBuilder")
public class FilePartContentSource implements ContentSource {

    public static final String CONTENT_SOURCE_TYPE = "reactiveFilePart";

    @NonNull
    private final FilePart file;

    private String fileName;

    private String fileVersion;

    private Map<String, String> metaInfo;

    @Override
    public String getContentSourceType() {
        return CONTENT_SOURCE_TYPE;
    }

    public static FilePartContentSource.FilePartContentSourceBuilder builder(@NonNull FilePart file) {
        return hiddenBuilder().file(file);
    }
}
