package io.logosflow.modules.app.contentrepository.resources.resolvers;

import io.logosflow.modules.app.contentrepository.models.content.ContentId;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @author Shkirmantsev
 */
@Component
public class MinioFileUuidV5IdTranslator extends AbstractUuidV5IdTranslator<ContentId> {

    public static final String NAMESPACE_ID_STR = "d0328055-d723-41f9-8f08-29d407771d6b";


    public MinioFileUuidV5IdTranslator() {
        super(ContentId::uri);
    }

    @Override
    public UUID namespace() {
        return UUID.fromString(NAMESPACE_ID_STR);
    }
}
