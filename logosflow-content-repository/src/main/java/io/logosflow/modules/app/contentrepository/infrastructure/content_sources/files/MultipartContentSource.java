package io.logosflow.modules.app.contentrepository.infrastructure.content_sources.files;

import io.logosflow.modules.app.contentrepository.infrastructure.content_sources.ContentSource;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * @author Shkirmantsev
 */
@Getter
@Builder(builderMethodName = "hiddenBuilder")
public class MultipartContentSource implements ContentSource {

    public static final String CONTENT_SOURCE_TYPE = "multipartFile";

    @NonNull
    private final MultipartFile file;

    private String fileName;

    private String fileVersion;

    private Map<String, String> metaInfo;

    @Override
    public String getContentSourceType() {
        return CONTENT_SOURCE_TYPE;
    }

    public static MultipartContentSourceBuilder builder(@NonNull MultipartFile file) {
        return hiddenBuilder().file(file);
    }
}
