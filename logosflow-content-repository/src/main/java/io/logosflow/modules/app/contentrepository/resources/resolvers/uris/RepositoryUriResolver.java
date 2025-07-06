package io.logosflow.modules.app.contentrepository.resources.resolvers.uris;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

/**
 * This resolver solves repository and corresponding resource URI by UUID, returns {@link URI} of the resource in the
 * found repository
 *
 * @author Shkirmantsev
 */
public interface RepositoryUriResolver {

    Optional<URI> resolve(UUID resourceId);

    Boolean add(UUID resourceId, URI uri);

    /**
     * Implementation must be thread safe
     */
    Boolean remove(UUID resourceId);

    /**
     * Implementation must be thread safe
     */
    Boolean remove(URI uri);

    void clear();
}
