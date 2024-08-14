package eu.okaeri.tasker.core;

import eu.okaeri.tasker.core.chain.TaskerChainAccessor;
import eu.okaeri.tasker.core.role.*;
import lombok.NonNull;

import java.util.function.*;

public class TaskerDsl {

    public static <T> TaskerConsumer<T> accept(@NonNull Consumer<T> consumer) {
        return new DefaultTaskerConsumer<>(consumer);
    }

    public static <T, R> TaskerFunction<T, R> transform(@NonNull Function<T, R> function) {
        return new DefaultTaskerFunction<>(function);
    }

    public static <T> TaskerPredicate<T> cond(@NonNull Predicate<T> predicate) {
        return new DefaultTaskerPredicate<>(predicate);
    }

    public static <T> TaskerPredicate<T> cond(@NonNull BooleanSupplier supplier) {
        return new DefaultTaskerPredicate<>(data -> supplier.getAsBoolean());
    }

    public static <T> TaskerRunnable<T> run(@NonNull Runnable runnable) {
        return new DefaultTaskerRunnable<>(runnable);
    }

    public static <T> TaskerSupplier<T> supply(@NonNull Supplier<T> supplier) {
        return new DefaultTaskerSupplier<>(supplier);
    }

    @SuppressWarnings("unchecked")
    public static <T> Taskerable<T> raw(@NonNull Consumer<TaskerChainAccessor> consumer) {
        return (Taskerable<T>) new DefaultTaskerUnsafe(consumer);
    }
}
