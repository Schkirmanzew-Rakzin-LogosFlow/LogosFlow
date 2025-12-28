package io.logosflow.modules.app.contentrepository.infrastructure.configs;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Shkirmantsev
 */
@Configuration
@RequiredArgsConstructor
public class MinioDBClientConfig {

    private final MinioDBProperties minioDBProperties;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minioDBProperties.url())
                .credentials(minioDBProperties.accessKey(), minioDBProperties.secretKey())
                .build();
    }
}
