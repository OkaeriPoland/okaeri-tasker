package eu.okaeri.tasker.core.chain;

import eu.okaeri.tasker.core.TaskerExecutor;
import lombok.NonNull;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class SharedChain<T> extends TaskerChain<T> {

    private final Queue<Runnable> queue;
    private final AtomicBoolean executed = new AtomicBoolean(false);

    public SharedChain(TaskerExecutor<?> executor, Queue<Runnable> queue) {
        super(executor);
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
