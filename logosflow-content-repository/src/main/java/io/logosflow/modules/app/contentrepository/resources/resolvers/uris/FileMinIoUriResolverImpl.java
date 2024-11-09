package io.logosflow.modules.app.contentrepository.resources.resolvers.uris;

import io.logosflow.modules.app.contentrepository.resources.resolvers.MinioFileUuidV5IdTranslator;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Shkirmantsev
 */
@Component
public class FileMinIoUriResolverImpl extends FileUriResolver {

    private static final String SCHEME = "urn";
    private static final String MINIO_FILES_NAMESPACE = MinioFileUuidV5IdTranslator.NAMESPACE;

    @Override
    public URI resolve(MultipartFile file) {
        String name = file.getName();
        String hash = generateFileHash(file);
        //urn:{MINIO_FILES_NAMESPACE}:file_name:hash
        String urn = String.format("%s:%s:%s:%s", SCHEME, MINIO_FILES_NAMESPACE, name, hash);
        try {
            return new URI(urn);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URI syntax", e);
        }
    }

    @Override
    public boolean isSuitable(URI uri) {
        return isMinioUri(uri);
    }

    private String generateFileHash(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            return DigestUtils.sha256Hex(inputStream);
        } catch (IOException e) {
            throw new FileHashGenerationException("Failed to generate hash for file: " + file.getOriginalFilename(), e);
        }
    }


    public boolean isMinioUri(URI uri) {
        String[] parts = uri.toString().split(":");
        return (parts.length == 4 || parts.length == 5)
                && SCHEME.equals(parts[0])
                && MINIO_FILES_NAMESPACE.equals(parts[1]);
    }

    public static class FileHashGenerationException extends RuntimeException {
        public FileHashGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
