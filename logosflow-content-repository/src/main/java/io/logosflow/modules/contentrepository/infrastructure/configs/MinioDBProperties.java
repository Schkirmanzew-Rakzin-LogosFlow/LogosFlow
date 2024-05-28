package io.logosflow.modules.app.contentrepository.infrastructure.configs;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author Shkirmantsev
 */
@Configuration
@Getter
@Accessors(fluent = true)
public class MinioDBProperties {

    @Value("${repositories.content.databases.minio.url}")
    private String url;

    @Value("${repositories.content.databases.minio.access-key}")
    private String accessKey;

    @Value("${repositories.content.databases.minio.secret-key}")
    private String secretKey;
}
