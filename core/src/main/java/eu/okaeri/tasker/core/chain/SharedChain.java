package eu.okaeri.tasker.core.chain;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.Delegate;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class SharedChain<T> extends TaskerChain<T> {

    protected @Delegate(excludes = Accessors.class) TaskerChain<T> delegate;
    protected final Queue<Runnable> queue;
    protected boolean executed = false;

    private interface Accessors<T> {
        void execute(@NonNull Consumer<T> consumer);
        void execute();
        Future<T> executeFuture();
        T await();
        T await(long timeout, TimeUnit unit);
    }

    public SharedChain(@NonNull TaskerChain<T> delegate, @NonNull Queue<Runnable> queue) {
        super(delegate.tasker);
        this.delegate = delegate;
        this.queue = queue;
    }

    protected void queueWith(@NonNull Runnable runnable) {
        if (this.executed) {
            throw new RuntimeException("Cannot execute already executed chain");
        }
        this.executed = true;
        this.queue.add(runnable);
    }

    @Override
    public void execute(@NonNull Consumer<T> consumer) {
        this.queueWith(() -> consumer.accept(this.delegate.await()));
    }

    @Override
    public void execute() {
        this.queueWith(this.delegate::await);
    }

    @Override
    public Future<T> executeFuture() {
        CompletableFuture<T> future = new CompletableFuture<>();
        this.queueWith(() -> future.complete(this.delegate.await()));
        return future;
    }

    @Override
    @SneakyThrows
    public T await() {
        return this.executeFuture().get();
    }

    @Override
    @SneakyThrows
    public T await(long timeout, TimeUnit unit) {
        return this.executeFuture().get(timeout, unit);
    }
}
