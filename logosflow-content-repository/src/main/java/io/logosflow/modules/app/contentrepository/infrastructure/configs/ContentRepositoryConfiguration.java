package io.logosflow.modules.app.contentrepository.infrastructure.configs;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({MinioDBProperties.class, ContentRepositoryProperties.class})
public class ContentRepositoryConfiguration {

//    @Bean
//    @ConditionalOnProperty(name = "content.repository.storage.type", havingValue = "minio", matchIfMissing = true)
//    public StorageBackend minioStorageBackend(MinioClient minioClient) {
//        return new MinioStorageBackend(minioClient);
//    }

    // Future: S3, FileSystem, etc.
}
