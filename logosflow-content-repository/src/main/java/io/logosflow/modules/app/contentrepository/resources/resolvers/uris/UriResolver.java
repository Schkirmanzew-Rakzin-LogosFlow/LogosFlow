package io.logosflow.modules.app.contentrepository.resources.resolvers.uris;

import java.net.URI;

/**
 * with help of this implementation the XxxUriResolver classes could define either they are responsible for
 * processing for passed {@link URI}
 *
 * @author Shkirmantsev
 */
public interface UriResolver<T> {

    URI resolve(T contentSourceObject);
}
