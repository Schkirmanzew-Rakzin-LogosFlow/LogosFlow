package io.logosflow.modules.app.contentrepository.resources.facade_impls;

import io.logosflow.modules.app.contentrepository.models.content.Content;
import io.logosflow.modules.app.contentrepository.models.content.ContentId;
import io.logosflow.modules.app.contentrepository.models.content.StreamingContent;
import io.logosflow.modules.app.contentrepository.resources.AllContentsRepositoryFacade;
import io.logosflow.modules.app.contentrepository.resources.ContentSpecificDedicatedRepository;
import io.logosflow.modules.app.contentrepository.resources.resolvers.uris.RepositoryUriResolver;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Shkirmantsev
 */
@Repository
@RequiredArgsConstructor
public class AllContentsRepositoryFacadeImpl implements AllContentsRepositoryFacade {

    private final List<ContentSpecificDedicatedRepository> repositories;
    private final RepositoryUriResolver repositoryUriResolver;

    @Override
    public String save(Content content) {
        return findSuitableRepository(content)
                .map(repository -> repository.save(content))
                .orElseThrow(() -> new ContentGeneralRepositoryException("No suitable repository found for content"));
    }

    @Override
    public Optional<Content> find(ContentId contentId) {
        return findSuitableRepository(contentId)
                .flatMap(repository -> repository.find(contentId));
    }

    @Override
    public Optional<Content> find(UUID resourceId) {
        return findSuitableRepository(resourceId)
                .flatMap(repository -> repository.find(resourceId));
    }

    @Override
    public Optional<String> checkExistingContent(UUID resourceId) {
        return findSuitableRepository(resourceId)
                .flatMap(repository -> repository.checkExistingContent(resourceId));
    }

    @Override
    public Mono<String> checkExistingContentAsynchronously(UUID resourceId) {
        return findSuitableRepository(resourceId)
                .map(repository -> repository.checkExistingContentAsynchronously(resourceId))
                .orElse(Mono.empty());
    }

    @Override
    public Mono<String> save(StreamingContent content) {
        return Mono.fromCallable(() -> findSuitableRepository(content))
                .flatMap(optional -> optional.map(Mono::just)
                        .orElseThrow(() ->
                                new ContentGeneralRepositoryException("No suitable repository found for content"))
                        .flatMap(repository -> repository.save(content)));
    }

    @Override
    public Mono<StreamingContent> findAsynchronously(ContentId contentId) {
        return Mono.defer(() -> findSuitableRepository(contentId)
                .map(repository -> repository.findAsynchronously(contentId))
                .orElse(Mono.empty()));
    }

    @Override
    public Mono<StreamingContent> findAsynchronously(UUID resourceId) {
        return Mono.defer(() -> findSuitableRepository(resourceId)
                .map(repository -> repository.findAsynchronously(resourceId))
                .orElse(Mono.empty()));
    }


    private @NonNull Optional<ContentSpecificDedicatedRepository> findSuitableRepository(Content content) {
        return repositories.stream()
                .filter(repository -> repository.isSuitable(content))
                .findFirst();
    }

    private @NonNull Optional<ContentSpecificDedicatedRepository> findSuitableRepository(StreamingContent content) {
        return repositories.stream()
                .filter(repository -> repository.isSuitable(content))
                .findFirst();
    }

    private @NonNull Optional<ContentSpecificDedicatedRepository> findSuitableRepository(UUID resourceId) {
        return repositoryUriResolver
                .resolve(resourceId)
                .flatMap(uri -> findSuitableRepository(() -> uri));
    }

    private @NonNull Optional<ContentSpecificDedicatedRepository> findSuitableRepository(ContentId contentId) {
        return repositories
                .stream()
                .filter(contentSpecificDedicatedRepository -> contentSpecificDedicatedRepository.isSuitable(contentId))
                .findFirst();
    }
}
