package io.logosflow.modules.app.contentrepository.infrastructure.contentsource.services;

import io.logosflow.modules.app.contentrepository.utils.CloseableGenericWrapper;
import io.minio.*;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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
    public Mono<String> streamingUpload(String bucketName, String resourceUriToSave, Flux<DataBuffer> dataBufferFlux,
                                        String mediaType) {
        return Mono.defer(() -> {
            PipedOutputStream outputStream = new PipedOutputStream();
            PipedInputStream inputStream;
            try {
                inputStream = new PipedInputStream(outputStream);
            } catch (IOException e) {
                return Mono.error(new RuntimeException("Error initializing piped streams", e));
            }

            // Write data to output stream and upload using input stream
            return dataBufferFlux
                    .map(this::getBytesFromBuffer)
                    .doOnNext(bytes -> writeToStream(outputStream, bytes))
                    .then()
                    .then(Mono.fromCallable(() -> performUpload(bucketName, resourceUriToSave, inputStream, mediaType)))
                    .doOnTerminate(() -> closeStreams(inputStream, outputStream))
                    .onErrorMap(e -> {
                        log.error("Streaming upload failed: {}", e.getMessage(), e);
                        return new RuntimeException("Error during streaming upload", e);
                    });
        });
    }

    private String performUpload(String bucketName, String resourceUriToSave, InputStream inputStream, String mediaType)
            throws Exception {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(resourceUriToSave)
                        .stream(inputStream, AUTODETECT_OBJECT_SIZE, AUTODETECT_PART_SIZE)
                        .contentType(mediaType)
                        .build()
        );
        return resourceUriToSave;
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


    private void closeStreams(Closeable... closeables) {
        for (Closeable closeable : closeables) {
            try {
                closeable.close();
            } catch (IOException e) {
                log.error("Error closing stream: {}", e.getMessage(), e);
            }
        }
    }


    @Override
    public Optional<String> isExists(String bucketName, String resourceUriToSave)
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

    static class CloseableGetObjectResponse extends CloseableGenericWrapper<GetObjectResponse> {

        public CloseableGetObjectResponse(GetObjectResponse closeableResource) {
            super(closeableResource);
        }
    }
}
