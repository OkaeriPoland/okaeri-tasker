package eu.okaeri.tasker.core.context;

import lombok.NonNull;

import java.time.Duration;

public interface TaskerContext {

    Object run(@NonNull Runnable runnable);

    Object runLater(@NonNull Duration delay, @NonNull Runnable runnable);

    Object schedule(@NonNull Runnable runnable);

    Object schedule(@NonNull Runnable runnable, @NonNull Duration delay, @NonNull Duration rate);

    default Object schedule(@NonNull Runnable runnable, @NonNull Duration rate) {
        return this.schedule(runnable, rate, rate);
    }
}
