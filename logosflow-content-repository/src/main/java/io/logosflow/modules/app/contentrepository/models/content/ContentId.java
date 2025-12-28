package io.logosflow.modules.app.contentrepository.models.content;

import lombok.NonNull;

import java.net.URI;

/**
 * @author Shkirmantsev
 */
@FunctionalInterface
public interface ContentId {

    @NonNull
    URI uri();
}