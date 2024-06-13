package io.logosflow.modules.app.contentrepository.resources.resolvers;

import io.logosflow.modules.app.contentrepository.models.content.ContentId;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @author Shkirmantsev
 */
@Component
public class UuidV5IdTranslatorDefault extends AbstractUuidV5IdTranslator<ContentId> {

    public UuidV5IdTranslatorDefault() {
        super(ContentId::uri);
    }

    @Override
    public UUID namespace() {
        return UUID.fromString(DEFAULT_NAMESPACE);
    }
}
