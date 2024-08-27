package eu.okaeri.tasker.core.role;

import eu.okaeri.tasker.core.Taskerable;
import lombok.NonNull;

import java.util.function.BiConsumer;

public interface TaskerBiConsumer<T, S> extends BiConsumer<T, S>, Taskerable<T> {
    TaskerBiConsumer<T, S> inputTwo(@NonNull String key);
    String inputTwo();
}
