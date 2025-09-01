package eu.okaeri.tasker.core.chain;

import eu.okaeri.tasker.core.Tasker;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.util.Queue;
import java.util.concurrent.*;
import java.util.function.Consumer;

import static eu.okaeri.tasker.core.chain.TaskerChainAccessor.DATA_EXCEPTION;

public class SharedChain<T> extends TaskerChain<T> {

    protected final Queue<Runnable> queue;
    protected boolean executed = false;

    public SharedChain(@NonNull Tasker tasker, @NonNull Queue<Runnable> queue) {
        super(tasker);
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
        this.queueWith(() -> consumer.accept(this.join()));
    }

    @Override
    public void execute() {
        this.queueWith(this::join);
    }

    @Override
    public CompletableFuture<T> executeFuture() {
        CompletableFuture<T> future = new CompletableFuture<>();
        this.queueWith(() -> future.complete(this.join()));
        return future;
    }

    @Override
    public T await() throws ExecutionException, InterruptedException {
        return this.executeFuture().get();
    }

    @Override
    public T await(long timeout, TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException {
        return this.executeFuture().get(timeout, unit);
    }

    @Override
    @SneakyThrows
    public T join() {
        try {
            return this.executeFuture0().join();
        }
        catch (CompletionException | CancellationException exception) {
            if (this.abort && !this.accessor.has(DATA_EXCEPTION)) {
                // handled
                return null;
            }
            throw exception;
        }
    }
}
