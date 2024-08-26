package eu.okaeri.tasker.core.context;

import lombok.NonNull;
import lombok.SneakyThrows;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.logging.Logger;

public class DefaultTaskerContext implements TaskerContext {

    public static final DefaultTaskerContext INSTANCE = new DefaultTaskerContext();
    private static final Logger LOGGER = Logger.getLogger(DefaultTaskerContext.class.getName());

    public static DefaultTaskerContext getInstance() {
        return INSTANCE;
    }

    @Override
    public Object run(@NonNull Runnable runnable) {
        LOGGER.warning("DefaultTaskerContext#run was called! This implementation is intended for testing only!");
        Thread thread = new Thread(runnable);
        thread.start();
        return thread;
    }

    @Override
    @SneakyThrows
    public Object runLater(@NonNull Duration delay, @NonNull Runnable runnable) {
        LOGGER.warning("DefaultTaskerContext#runLater was called! This implementation is intended for testing only!");
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(delay.toMillis());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            runnable.run();
        });
        thread.start();
        return thread;
    }

    @Override
    @SuppressWarnings("BusyWait")
    public Object schedule(@NonNull Runnable runnable) {
        LOGGER.warning("DefaultTaskerContext#schedule was called! This implementation is intended for testing only!");
        Thread thread = new CloseableThread() {
            @Override
            public void run() {
                while (!this.closed) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    runnable.run();
                }
            }
        };
        thread.start();
        return thread;
    }

    @Override
    public Object schedule(@NonNull Runnable runnable, @NonNull Duration delay, @NonNull Duration rate) {
        return this.schedule(runnable);
    }

    protected static class CloseableThread extends Thread implements Closeable {

        protected volatile boolean closed = false;

        @Override
        public void close() throws IOException {
            this.closed = true;
        }
    }
}
