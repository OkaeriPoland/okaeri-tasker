package eu.okaeri.tasker.core.context;

import lombok.NonNull;
import lombok.SneakyThrows;

import java.time.Duration;

public class DefaultTaskerContext implements TaskerContext {

    public static final DefaultTaskerContext INSTANCE = new DefaultTaskerContext();

    public static DefaultTaskerContext getInstance() {
        return INSTANCE;
    }

    @Override
    public Object run(@NonNull Runnable runnable) {
        runnable.run();
        return null;
    }

    @Override
    @SneakyThrows
    public Object runLater(@NonNull Duration delay, @NonNull Runnable runnable) {
        Thread.sleep(delay.toMillis());
        runnable.run();
        return null;
    }

    @Override
    public Object schedule(@NonNull Runnable runnable) {
        throw new RuntimeException("Not implemented yet!");
    }

    @Override
    public Object schedule(@NonNull Runnable runnable, @NonNull Duration delay, @NonNull Duration rate) {
        throw new RuntimeException("Not implemented yet!");
    }
}
