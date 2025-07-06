package io.logosflow.modules.app.contentrepository.utils;

import lombok.RequiredArgsConstructor;

import java.io.Closeable;
import java.io.IOException;

@RequiredArgsConstructor
public abstract class CloseableGenericWrapper<T> implements Closeable {

    private final T closeableResource;

    @Override
    public void close() throws IOException {

    }

    public T get() {
        return closeableResource;
    }
}
