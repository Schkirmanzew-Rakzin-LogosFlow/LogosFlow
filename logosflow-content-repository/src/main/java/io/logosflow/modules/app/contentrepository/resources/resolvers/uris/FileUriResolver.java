package io.logosflow.modules.app.contentrepository.resources.resolvers.uris;

import org.springframework.web.multipart.MultipartFile;

import java.net.URI;

/**
 * @author Shkirmantsev
 */
public abstract class FileUriResolver implements UriResolver<MultipartFile> {

    public abstract boolean isSuitable(URI uri);
}
