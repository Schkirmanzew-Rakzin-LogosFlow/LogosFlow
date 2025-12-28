package io.logosflow.modules.app.contentrepository.infrastructure.health;

import io.logosflow.modules.app.contentrepository.infrastructure.content_sources.services.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContentRepositoryHealthIndicator implements HealthIndicator {

    private final MinioService minioService;

    @Override
    public Health health() {
        try {
            // Test MinIO connectivity
            minioService.chekExistingContent("health-check", "test");
            return Health.up()
                    .withDetail("storage", "MinIO accessible")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("storage", "MinIO not accessible")
                    .withException(e)
                    .build();
        }
    }
}
