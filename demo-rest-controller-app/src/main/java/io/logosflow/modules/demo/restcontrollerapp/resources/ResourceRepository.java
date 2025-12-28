package io.logosflow.modules.demo.restcontrollerapp.resources;

import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.stereotype.Repository;

import java.time.Instant;

/**
 * @author Shkirmantsev
 */
@Repository
@Data
public class ResourceRepository {

    @SneakyThrows
    public String getResourceByTiming(String resourceId, Long timeout) {
        var started = Instant.now();

        Thread.sleep(timeout);

        return "Resource id=" + resourceId + "" +
                " with timing=" + timeout + "" +
                " threadName = " + Thread.currentThread().getName() +
                " started at " + started;
    }
}
