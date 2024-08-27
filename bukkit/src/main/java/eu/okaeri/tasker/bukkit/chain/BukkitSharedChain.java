package eu.okaeri.tasker.bukkit.chain;

import eu.okaeri.tasker.bukkit.BukkitTasker;
import lombok.NonNull;
import lombok.experimental.Delegate;

import java.util.Queue;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class BukkitSharedChain<T> extends BukkitTaskerChain<T> {

    protected @Delegate(excludes = Accessors.class) BukkitTaskerChain<T> delegate;
    protected final Queue<Runnable> queue;
    protected boolean executed = false;

    private interface Accessors<T> {
        void execute(@NonNull Consumer<T> consumer);
        void execute();
        Future<T> executeFuture();
        T await() throws ExecutionException, InterruptedException;
        T await(long timeout, TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException;
        void join();
    }

    public BukkitSharedChain(@NonNull BukkitTaskerChain<T> delegate, @NonNull Queue<Runnable> queue) {
        super(((BukkitTasker) delegate.getTasker()));
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
        this.queueWith(() -> consumer.accept(this.delegate.join()));
    }

    @Override
    public void execute() {
        this.queueWith(this.delegate::join);
    }

    @Override
    public CompletableFuture<T> executeFuture() {
        CompletableFuture<T> future = new CompletableFuture<>();
        this.queueWith(() -> future.complete(this.delegate.join()));
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
    public T join() {
        return this.executeFuture().join();
    }
}
