package io.logosflow.modules.app.contentrepository.resources.dedicated_impls;

import io.logosflow.modules.app.contentrepository.infrastructure.content_sources.services.MinioService;
import io.logosflow.modules.app.contentrepository.models.content.Content;
import io.logosflow.modules.app.contentrepository.models.content.ContentId;
import io.logosflow.modules.app.contentrepository.models.content.StreamingContent;
import io.logosflow.modules.app.contentrepository.models.content.impls.DefaultContent;
import io.logosflow.modules.app.contentrepository.resources.resolvers.MinioFileUuidV5IdTranslator;
import io.logosflow.modules.app.contentrepository.resources.resolvers.uris.FileUriResolver;
import org.springframework.stereotype.Repository;

import java.net.URI;

/**
 * @author Shkirmantsev
 */
@Repository
public class MinIoFileContentsDedicatedRepository extends MinIoSpecialContentDedicatedAbstractRepository {

    protected final MinioFileUuidV5IdTranslator minioFileUuidV5IdTranslator;
    private final FileUriResolver fileUriResolver;

    public MinIoFileContentsDedicatedRepository(
            MinioService service,
            MinioFileUuidV5IdTranslator minioFileUuidV5IdTranslator,
            FileUriResolver fileUriResolver
    ) {
        super(service, minioFileUuidV5IdTranslator);
        this.minioFileUuidV5IdTranslator = minioFileUuidV5IdTranslator;
        this.fileUriResolver = fileUriResolver;
    }

    @Override
    public boolean isSuitable(Content content) {
        return content instanceof DefaultContent;
    }

    @Override
    public boolean isSuitable(StreamingContent content) {
        return isMinioUri(content.getId().uri());
    }

    @Override
    public boolean isSuitable(ContentId contentId) {
        return isMinioUri(contentId.uri());
    }

    private boolean isMinioUri(URI uri) {
        return fileUriResolver.isSuitable(uri);
    }

    @Override
    protected String bucketName() {
        return MinioFileUuidV5IdTranslator.NAMESPACE_ID_STR;
    }
}
