package eu.okaeri.tasker.core;

import lombok.NonNull;

import java.time.Duration;

public interface TaskerExecutor<T> {

    boolean isMain();

    T schedule(@NonNull Runnable runnable, boolean async);

    T schedule(@NonNull Runnable runnable, @NonNull Duration delay, @NonNull Duration rate, boolean async);

    default T schedule(@NonNull Runnable runnable, @NonNull Duration rate, boolean async) {
        return this.schedule(runnable, rate, rate, async);
    }

    T run(@NonNull Runnable runnable, @NonNull Runnable callback, boolean async);

    void cancel(@NonNull T task);
}
