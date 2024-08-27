package eu.okaeri.tasker.core.role;

import eu.okaeri.tasker.core.Taskerable;
import lombok.NonNull;

import java.util.function.BiFunction;

public interface TaskerBiFunction<T, S, R> extends BiFunction<T, S, R>, Taskerable<R> {
    TaskerBiFunction<T, S, R> inputTwo(@NonNull String key);
    String inputTwo();
}
