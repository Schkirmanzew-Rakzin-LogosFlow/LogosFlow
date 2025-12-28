package io.logosflow.modules.app.contentrepository.infrastructure.content_sources.services;

import io.logosflow.modules.app.contentrepository.models.content.Content;
import io.logosflow.modules.app.contentrepository.models.content.ContentId;
import io.logosflow.modules.app.contentrepository.models.content.StreamingContent;
import io.minio.ObjectWriteResponse;
import io.minio.errors.*;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author Shkirmantsev
 */
public interface MinioService {

    //Todo version with headers
    ObjectWriteResponse upload(
            String bucketName,
            Supplier<InputStream> data,
            Long dataSizeInBytes,
            String resourceUriToSave,
            MediaType contentType,
            Map<String, String> metaInfo
    ) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException,
            InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;

    //ContentMetaInfoConstants.USED_DEFAULT_MEDIA_TYPE_VALUE
    Mono<ObjectWriteResponse> streamingUpload(String bucketName,
                                              String resourceUriToSave,
                                              Supplier<Flux<DataBuffer>> dataBufferFlux,
                                              String mediaType,
                                              Map<String, String> metaInfo
    );

    Optional<Content> find(ContentId contentId);

    Optional<String> chekExistingContent(String bucketName, String resourceUriToSave)
            throws ServerException, InsufficientDataException, ErrorResponseException, IOException,
            NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException,
            InternalException;

    Optional<Content> findBy(String bucketName, String resourceIdStr);

    Optional<StreamingContent> findAsyncBy(ContentId contentId);

    Mono<StreamingContent> findAsynchronouslyBy(String bucketName, String resourceIdStr);
}
