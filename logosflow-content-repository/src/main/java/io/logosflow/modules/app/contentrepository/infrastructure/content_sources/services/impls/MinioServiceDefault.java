package io.logosflow.modules.app.contentrepository.infrastructure.content_sources.services.impls;

import io.logosflow.modules.app.contentrepository.infrastructure.content_sources.services.MinioService;
import io.logosflow.modules.app.contentrepository.models.content.Content;
import io.logosflow.modules.app.contentrepository.models.content.ContentId;
import io.logosflow.modules.app.contentrepository.models.content.StreamingContent;
import io.logosflow.modules.app.contentrepository.models.content.impls.DefaultContent;
import io.logosflow.modules.app.contentrepository.models.content.impls.FluxStreamingContent;
import io.logosflow.modules.app.contentrepository.resources.resolvers.UuidV5IdTranslatorDefault;
import io.logosflow.modules.app.contentrepository.utils.CloseableGenericWrapper;
import io.minio.*;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.*;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * MinIO Service Implementation for content management.
 * Handles object uploads and existence checks.
 *
 * @author Shkirmantsev
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MinioServiceDefault implements MinioService {

    private static final String GENERAL_ERROR_MESSAGE_TEXT = "Error uploading to MinIO";
    private static final int AUTODETECT_OBJECT_SIZE = -1;
    private static final int AUTODETECT_PART_SIZE = -1;

    private final MinioClient minioClient;
    private final UuidV5IdTranslatorDefault uuidV5IdTranslator;

    @Override
    public ObjectWriteResponse upload(
            String bucketName,
            Supplier<InputStream> dataSupplier,
            Long dataSizeInBytes,
            String resourceUriToSave,
            MediaType contentType,
            Map<String, String> metaInfo
    ) throws
            IOException,
            ServerException,
            InsufficientDataException,
            ErrorResponseException,
            NoSuchAlgorithmException,
            InvalidKeyException,
            InvalidResponseException,
            XmlParserException,
            InternalException {

        try (InputStream dataStream = dataSupplier.get()) {
            return minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(resourceUriToSave)
                            .stream(dataStream, dataSizeInBytes, AUTODETECT_PART_SIZE)
                            .contentType(contentType.toString())
                            .userMetadata(metaInfo)
                            .build()
            );
        }
    }

    @Override
    public Mono<ObjectWriteResponse> streamingUpload(String bucketName, String resourceUriToSave,
                                                     Supplier<Flux<DataBuffer>> dataBufferFlux,
                                                     String mediaType,
                                                     Map<String, String> metaInfo
    ) {
        return Mono.defer(() -> {
            PipedOutputStream outputStream = new PipedOutputStream();
            PipedInputStream inputStream;
            try {
                inputStream = new PipedInputStream(outputStream);
            } catch (IOException e) {
                return Mono.error(new RuntimeException("Error initializing piped streams", e));
            }

            // Write data to output stream and upload using input stream
            return dataBufferFlux.get()
                    .doOnError(error -> closeStreams(inputStream, outputStream)) // Add error cleanup
                    .doOnCancel(() -> closeStreams(inputStream, outputStream))   // Add cancellation
                    .map(this::getBytesFromBuffer)
                    .doOnNext(bytes -> writeToStream(outputStream, bytes))
                    .doOnComplete(() -> {
                        try {
                            outputStream.close(); // Signal end of data
                        } catch (IOException e) {
                            log.warn("Error closing output stream", e);
                        }
                    })
                    .then()
                    .then(Mono.fromCallable(() ->
                            performUpload(bucketName, resourceUriToSave, inputStream, mediaType, metaInfo)))
                    .doOnTerminate(() -> closeStreams(inputStream, outputStream))
                    .onErrorMap(e -> {
                        log.error("Streaming upload failed: {}", e.getMessage(), e);
                        return new RuntimeException("Error during streaming upload", e);
                    });
        });
    }

    @Override
    public Optional<Content> find(ContentId contentId) {
        try {
            String resourceId = uuidV5IdTranslator.translate(contentId).toString();
            return findBy(bucketName(), resourceId);
        } catch (Exception e) {
            log.error("Error finding content by ContentId", e);
            return Optional.empty();
        }
    }



    @Override
    public Optional<String> chekExistingContent(String bucketName, String resourceUriToSave)
            throws
            ServerException,
            InsufficientDataException,
            ErrorResponseException,
            IOException,
            NoSuchAlgorithmException,
            InvalidKeyException,
            InvalidResponseException,
            XmlParserException,
            InternalException {

        try (var closeableGetObjectResponse = new CloseableGetObjectResponse(minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(resourceUriToSave)
                        .build()))) {

            return Optional.of(closeableGetObjectResponse)
                    .map(CloseableGenericWrapper::get)
                    .map(GetObjectResponse::object)
                    .map(String::strip);
        }
    }

    @Override
    public Optional<Content> findBy(String bucketName, String resourceIdStr) {
        try {
            return Optional.of(download(bucketName, resourceIdStr));
        } catch (Exception e) {
            log.debug("Content not found: bucket={}, resourceId={}", bucketName, resourceIdStr);
            return Optional.empty();
        }
    }

    @Override
    public Optional<StreamingContent> findAsyncBy(ContentId contentId) {
        try {
            String resourceId = uuidV5IdTranslator.translate(contentId).toString();
            return findAsynchronouslyBy(bucketName(), resourceId).blockOptional();
        } catch (Exception e) {
            log.error("Error finding async content by ContentId", e);
            return Optional.empty();
        }
    }

    @Override
    public Mono<StreamingContent> findAsynchronouslyBy(String bucketName, String resourceIdStr) {
        return downloadFlux(bucketName, resourceIdStr)
                .onErrorResume(e -> {
                    log.debug("Streaming content not found: bucket={}, resourceId={}", bucketName, resourceIdStr);
                    return Mono.empty();
                });
    }

    Mono<StreamingContent> downloadFlux(
            String bucketName,
            String resourceUriToSave
    ) {
        return Mono.fromCallable(() ->
                        minioClient.getObject(
                                GetObjectArgs.builder()
                                        .bucket(bucketName)
                                        .object(resourceUriToSave)
                                        .build()
                        ))
                .map(getObjectResponse ->

                        FluxStreamingContent.builder()
                                .mediaType(MediaType.APPLICATION_OCTET_STREAM)
                                .dataSupplier(() -> DataBufferUtils.readInputStream(
                                        () -> getObjectResponse,
                                        new DefaultDataBufferFactory(),
                                        4096 // buffer size
                                ))
                                .metaInfo(Collections.emptyMap()) //TODO minio user info
                                //TODO: Minio URI to ID
                                .id(() -> URI.create("todoContentId"))
                                .dataSizeInBytesOrNull(
                                        Optional.ofNullable(getObjectResponse.headers().get("Content-Length"))
                                                .map(Long::valueOf)
                                                .orElse(null))
                                .build()
                );

    }

    Content download(
            String bucketName,
            String resourceUriToSave
    ) throws ServerException,
            InsufficientDataException,
            ErrorResponseException,
            IOException,
            NoSuchAlgorithmException,
            InvalidKeyException,
            InvalidResponseException,
            XmlParserException,
            InternalException {
        GetObjectResponse response = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(resourceUriToSave)
                        .build()
        );


        return DefaultContent
                .builder()
                .mediaType(MediaType.APPLICATION_OCTET_STREAM) //TODO: extract real content type
                .dataSupplier(() -> response)
                .metaInfo(Collections.emptyMap()) //TODO minio user info
                //TODO: Minio URI to ID
                .id(() -> URI.create("todoContentId"))
                .dataSizeInBytesOrNull(
                        Optional.ofNullable(response.headers().get("Content-Length"))
                                .map(Long::valueOf)
                                .orElse(null))
                .build();
    }

    private void closeStreams(Closeable... closeables) {
        for (Closeable closeable : closeables) {
            try {
                closeable.close();
            } catch (IOException e) {
                log.error("Error closing stream: {}", e.getMessage(), e);
            }
        }
    }

    private byte[] getBytesFromBuffer(DataBuffer dataBuffer) {
        try {
            byte[] bytes = new byte[dataBuffer.readableByteCount()];
            dataBuffer.read(bytes);
            return bytes;
        } finally {
            DataBufferUtils.release(dataBuffer);
        }
    }

    private void writeToStream(PipedOutputStream outputStream, byte[] bytes) {
        try {
            outputStream.write(bytes);
            outputStream.flush();
        } catch (IOException e) {
            throw new UncheckedIOException("Error writing to PipedOutputStream", e);
        }
    }

    private ObjectWriteResponse performUpload(
            String bucketName,
            String resourceUriToSave,
            InputStream inputStream,
            String mediaType,
            Map<String, String> metaInfo
    )
            throws Exception {
        return minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(resourceUriToSave)
                        .stream(inputStream, AUTODETECT_OBJECT_SIZE, AUTODETECT_PART_SIZE)
                        .contentType(mediaType)
                        .userMetadata(metaInfo)
                        .build()
        );
    }

    static class CloseableGetObjectResponse extends CloseableGenericWrapper<GetObjectResponse> {

        public CloseableGetObjectResponse(GetObjectResponse closeableResource) {
            super(closeableResource);
        }

        @Override
        public void close() throws IOException {
            get().close();
        }
    }

    private String bucketName() {
        return uuidV5IdTranslator.namespace().toString();
    }
}
