package eu.okaeri.tasker.core;

import eu.okaeri.tasker.core.chain.TaskerChainAccessor;
import eu.okaeri.tasker.core.context.TaskerContext;
import lombok.NonNull;

import java.util.function.Supplier;

public interface Taskerable<T> {

    Taskerable<T> context(@NonNull Supplier<TaskerContext> contextSupplier);
    TaskerContext context();

    Taskerable<T> input(@NonNull String key);
    String input();

    Taskerable<T> output(@NonNull String key);
    String output();

    Runnable call(@NonNull TaskerChainAccessor accessor);
}
