package io.logosflow.modules.app.contentrepository.infrastructure.contentsource.services;

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
    Mono<String> streamingUpload(String bucketName,
                                 String resourceUriToSave,
                                 Flux<DataBuffer> dataBufferFlux,
                                 String mediaType);

//    Optional<Content> find(Content.Id contentId);

    Optional<String> isExists(String bucketName, String resourceUriToSave)
            throws ServerException, InsufficientDataException, ErrorResponseException, IOException,
            NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException,
            InternalException;
}
