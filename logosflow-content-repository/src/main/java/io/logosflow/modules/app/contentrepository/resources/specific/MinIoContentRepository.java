package io.logosflow.modules.app.contentrepository.resources.specific;

import io.logosflow.modules.app.contentrepository.infrastructure.content.services.MinioService;
import io.logosflow.modules.app.contentrepository.models.content.Content;
import io.logosflow.modules.app.contentrepository.models.content.DefaultContent;
import io.logosflow.modules.app.contentrepository.resources.ContentSpecificRepository;
import io.logosflow.modules.app.contentrepository.resources.resolvers.UuidV5IdTranslatorDefault;
import io.minio.ObjectWriteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Shkirmantsev
 */
@Repository
@RequiredArgsConstructor
public class MinIoContentRepository implements ContentSpecificRepository {

    private final MinioService service;
    private final UuidV5IdTranslatorDefault uuidV5IdTranslatorDefault;

    @Override
    public String save(Content content) {

        try {
            ObjectWriteResponse response = service.upload(
                    content.getDataSupplier(),
                    uuidV5IdTranslatorDefault.translate(content.getId()).toString(),
                    content.getMediaType(),
                    content.getMetaInfo()
            );

            return response.object();
        } catch (Exception e) {
            throw new ContentSpecificRepositoryException(e);
        }
    }

    @Override
    public Optional<String> isExist(Content.Id contentId) {
        try {
            return service.isExists(uuidV5IdTranslatorDefault.translate(contentId).toString());
        } catch (Exception e) {
            throw new ContentSpecificRepositoryException(e);
        }
    }

    @Override
    public boolean isSuitable(Content content) {
        return content instanceof DefaultContent;
    }
}
