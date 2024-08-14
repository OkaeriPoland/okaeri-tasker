package eu.okaeri.tasker.core.chain;

import eu.okaeri.tasker.core.context.TaskerPlatform;
import lombok.NonNull;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class SharedChain<T> extends TaskerChain<T> {

    protected final Queue<Runnable> queue;
    protected final AtomicBoolean executed = new AtomicBoolean(false);

    public SharedChain(@NonNull TaskerPlatform platform, @NonNull Queue<Runnable> queue) {
        super(platform);
        this.queue = queue;
    }

    @Override
    public void execute() {
        if (this.executed.get()) {
            throw new RuntimeException("Cannot execute already executed chain");
        }
        this.executed.set(true);
        this.queue.add(super::await);
    }

    @Override
    public void execute(@NonNull Consumer<T> consumer) {
        if (this.executed.get()) {
            throw new RuntimeException("Cannot execute already executed chain");
        }
        this.executed.set(true);
        this.queue.add(() -> consumer.accept(super.await()));
    }
}
