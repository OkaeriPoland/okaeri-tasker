package eu.okaeri.tasker.core.role;

import eu.okaeri.tasker.core.Taskerable;

import java.util.function.Function;

public interface TaskerFunction<T, R> extends Function<T, R>, Taskerable<R> {
}
