package io.logosflow.modules.app.contentrepository.infrastructure.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Shkirmantsev
 */
@ConfigurationProperties(prefix = "content.repository.storage")
public record ContentRepositoryProperties(
        String type
) {
}
