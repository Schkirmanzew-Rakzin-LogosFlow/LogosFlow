package io.logosflow.modules.app.contentrepository.resources.resolvers.uris;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Component;

/**
 * This interface is used for resolving URI from different contents, thanks to delegation pattern (delegates to
 * different resolvers, like {@link FileUriResolver})
 *
 * @author Shkirmantsev
 * @see FileUriResolver
 */
@Component
@RequiredArgsConstructor
public class UriResolverFacade {

    @Delegate(types = FileUriResolver.class) //like: URI FileUriResolver.resolve(MultipartFile file)
    private final FileUriResolver fileUriResolver;

    //Here could be other resolvers to delegate code
}
