package io.logosflow.modules.app.contentrepository.resources.dedicated_impls;

import io.logosflow.modules.app.contentrepository.infrastructure.content_sources.services.MinioService;
import io.logosflow.modules.app.contentrepository.models.content.Content;
import io.logosflow.modules.app.contentrepository.models.content.ContentId;
import io.logosflow.modules.app.contentrepository.models.content.StreamingContent;
import io.logosflow.modules.app.contentrepository.resources.ContentSpecificDedicatedRepository;
import io.logosflow.modules.app.contentrepository.resources.resolvers.AbstractUuidV5IdTranslator;
import io.minio.GenericResponse;
import io.minio.ObjectWriteResponse;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

/**
 * @author Shkirmantsev
 */
@RequiredArgsConstructor
public abstract class MinIoSpecialContentDedicatedAbstractRepository implements ContentSpecificDedicatedRepository {

    private final MinioService service;
    protected final AbstractUuidV5IdTranslator<ContentId> uuidV5IdTranslator;

    @Override
    public String save(Content content) {

        try {
            ObjectWriteResponse response = service.upload(
                    bucketName(),
                    content.getDataSupplier(),
                    content.getDataSizeInBytes().filter(dsize -> dsize > 0).orElse(-1L), //see minio Docu
                    uuidV5IdTranslator.translate(content.getId()).toString(),
                    content.getMediaType(),
                    content.getMetaInfo()
            );

            return response.object();
        } catch (Exception e) {
            throw new ContentSpecificRepositoryException(e);
        }
    }

    @Override
    public Mono<String> save(StreamingContent content) {

        try {
            return service.streamingUpload(
                            bucketName(),
                            uuidV5IdTranslator.translate(content.getId()).toString(),
                            content.getDataSupplier(),
                            content.getMediaType().toString(),
                            content.getMetaInfo()
                    )
                    .map(GenericResponse::object);
        } catch (Exception e) {
            throw new ContentSpecificRepositoryException(e);
        }
    }


    @Override
    public Optional<String> checkExistingContent(UUID resourceId) {
        try {
            return service.chekExistingContent(bucketName(), resourceId.toString());
        } catch (Exception e) {
            throw new ContentSpecificRepositoryException(e);
        }
    }

    @Override
    public Optional<Content> find(ContentId contentId) {
        return find(uuidV5IdTranslator.translate(contentId));
    }

    @Override
    public Optional<Content> find(UUID resourceId) {
        return service.findBy(bucketName(), resourceId.toString());
    }

    @Override
    public Mono<StreamingContent> findAsynchronously(ContentId contentId) {
        return findAsynchronously(uuidV5IdTranslator.translate(contentId));
    }

    @Override
    public Mono<StreamingContent> findAsynchronously(UUID resourceId) {
        return service.findAsynchronouslyBy(bucketName(), resourceId.toString());
    }


    protected abstract String bucketName();
}
