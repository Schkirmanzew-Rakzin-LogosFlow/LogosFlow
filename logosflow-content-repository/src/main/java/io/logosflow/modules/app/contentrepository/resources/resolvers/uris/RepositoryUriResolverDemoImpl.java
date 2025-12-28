package io.logosflow.modules.app.contentrepository.resources.resolvers.uris;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 * @author Shkirmantsev
 */
@Slf4j
@Component
//TODO: listener/subscribe to the change events, DB, redis, bloom filter ect..
public class RepositoryUriResolverDemoImpl implements RepositoryUriResolver {
    private final RepoUriResolverCache cache = new RepoUriResolverCache();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private <T> T withReadLock(Supplier<T> supplier) {
        lock.readLock().lock();
        try {
            return supplier.get();
        } finally {
            lock.readLock().unlock();
        }
    }

    private <T> T withWriteLock(Supplier<T> supplier) {
        lock.writeLock().lock();
        try {
            return supplier.get();
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void withWriteLock(Runnable runnable) {
        lock.writeLock().lock();
        try {
            runnable.run();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<URI> resolve(UUID resourceId) {
        return withReadLock(() -> {
            log.debug("Resolving URI for resourceId: {}", resourceId);
            return cache.resolve(resourceId);
        });
    }

    @Override
    public Boolean add(UUID resourceId, URI uri) {
        return withWriteLock(() -> {
            log.debug("Adding resourceId: {} and URI: {} to cache", resourceId, uri);
            return cache.add(resourceId, uri);
        });
    }

    @Override
    public Boolean remove(UUID resourceId) {
        return withWriteLock(() -> {
            log.debug("Removing resourceId: {} from cache", resourceId);
            return cache.remove(resourceId);
        });
    }

    @Override
    public Boolean remove(URI uri) {
        return withWriteLock(() -> {
            log.debug("Removing URI: {} from cache", uri);
            return cache.remove(uri);
        });
    }

    @Override
    public void clear() {
        withWriteLock(() -> {
            log.debug("Clearing cache");
            cache.clear();
        });
    }

    @RequiredArgsConstructor
    private static class RepoUriResolverCache {
        private final Map<UUID, URI> mapUuidToUri = new ConcurrentHashMap<>();
        private final Map<URI, UUID> mapUriToUuid = new ConcurrentHashMap<>();

        public Optional<URI> resolve(UUID resourceId) {
            if (resourceId == null) {
                throw new NullPointerException("resourceId cannot be null");
            }
            return Optional.ofNullable(mapUuidToUri.get(resourceId));
        }

        //@Transactional
        public boolean add(UUID resourceId, URI uri) {
            if (resourceId == null || uri == null) {
                return false;
            }
            mapUuidToUri.put(resourceId, uri);
            mapUriToUuid.put(uri, resourceId);

            return true;
        }

        public boolean remove(UUID resourceId) {
            return Optional.ofNullable(resourceId)
                    .map(mapUuidToUri::remove)
                    .map(mapUriToUuid::remove)
                    .isPresent();
        }

        public boolean remove(URI resourceId) {
            return Optional.ofNullable(resourceId)
                    .map(mapUriToUuid::remove)
                    .map(mapUuidToUri::remove)
                    .isPresent();
        }

        public void clear() {
            mapUuidToUri.clear();
            mapUriToUuid.clear();
        }
    }
}
