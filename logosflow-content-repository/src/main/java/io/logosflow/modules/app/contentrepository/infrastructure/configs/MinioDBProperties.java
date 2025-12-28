package io.logosflow.modules.app.contentrepository.infrastructure.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Shkirmantsev
 */
@ConfigurationProperties(prefix = "repositories.content.databases.minio")
public record MinioDBProperties(
        String url,
        String accessKey,
        String secretKey
) {
}
