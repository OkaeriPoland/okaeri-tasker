package eu.okaeri.tasker.bukkit.chain;

import eu.okaeri.tasker.bukkit.BukkitTasker;
import lombok.NonNull;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class BukkitSharedChain<T> extends BukkitTaskerChain<T> {

    protected final Queue<Runnable> queue;
    protected final AtomicBoolean executed = new AtomicBoolean(false);

    public BukkitSharedChain(@NonNull BukkitTasker tasker, @NonNull Queue<Runnable> queue) {
        super(tasker);
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
