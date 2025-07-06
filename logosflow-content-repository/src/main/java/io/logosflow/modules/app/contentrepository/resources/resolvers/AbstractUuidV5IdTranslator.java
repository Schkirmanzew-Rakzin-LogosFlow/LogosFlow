package io.logosflow.modules.app.contentrepository.resources.resolvers;

import com.fasterxml.uuid.UUIDType;
import com.fasterxml.uuid.impl.NameBasedGenerator;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;

import java.net.URI;
import java.util.UUID;
import java.util.function.Function;

/**
 * @author Shkirmantsev
 */
@RequiredArgsConstructor
public abstract class AbstractUuidV5IdTranslator<ID> {

    public static final String DEFAULT_NAMESPACE = "c7c55f46-ac04-42d1-b41f-1ecd1769950b";

    @NonNull
    private final Function<ID, URI> extractor;

    public UUID translate(@NonNull ID id) {
        String uri = extractor.apply(id).toString();
        return uuidV5From(uri);
    }

    private UUID uuidV5From(String name) {
        return generator().generate(name);
    }

    private NameBasedGenerator generator() {

        return new NameBasedGenerator(
                namespace(),
                DigestUtils.getSha1Digest(),
                UUIDType.NAME_BASED_SHA1
        );
    }

    public abstract UUID namespace();
}
