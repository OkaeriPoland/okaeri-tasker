package eu.okaeri.tasker.core.chain;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.function.Supplier;

import static eu.okaeri.tasker.core.chain.TaskerChain.NOOP_RUNNABLE;

@SuppressWarnings("unchecked")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class TaskerChainAccessor {

    private final TaskerChain<?> chain;

    public void abort(boolean abort) {
        this.chain.abort.set(abort);
    }

    public boolean abort() {
        return this.chain.abort.get();
    }

    public void data(Object newValue) {
        this.chain.data.set(newValue);
    }

    public <D> D data() {
        return (D) this.chain.data.get();
    }

    public <D> D dataOr(D other) {
        D data = this.data();
        return data == null ? other : data;
    }

    public boolean lastAsync() {
        return this.chain.lastAsync.get();
    }

    // TASK
    public void taskInsert(@NonNull Runnable runnable, @NonNull Supplier<Boolean> async) {
        this.chain.tasks.add(
            this.chain.currentTaskIndex.get() + 1,
            new ChainTask(runnable, Duration.ZERO, async, false)
        );
    }

    public void task(@NonNull Runnable runnable, @NonNull Supplier<Boolean> async) {
        if (this.chain.executed.get()) {
            throw new RuntimeException("Cannot modify already executed chain");
        }
        this.chain.tasks.add(new ChainTask(runnable, Duration.ZERO, async, false));
    }

    public void taskInsert(@NonNull Runnable runnable) {
        this.taskInsert(runnable, this::lastAsync);
    }

    public void task(@NonNull Runnable runnable) {
        this.task(runnable, this::lastAsync);
    }

    public void syncInsert(@NonNull Runnable runnable) {
        this.taskInsert(runnable, () -> false);
    }

    public void sync(@NonNull Runnable runnable) {
        this.task(runnable, () -> false);
    }

    public void asyncInsert(@NonNull Runnable runnable) {
        this.taskInsert(runnable, () -> true);
    }

    public void async(@NonNull Runnable runnable) {
        this.task(runnable, () -> true);
    }

    // DELAY
    public void delayInsert(@NonNull Duration duration) {
        this.chain.tasks.add(
            this.chain.currentTaskIndex.get() + 1,
            new ChainTask(NOOP_RUNNABLE, duration, this::lastAsync, false)
        );
    }

    public void delay(@NonNull Duration duration) {
        if (this.chain.executed.get()) {
            throw new RuntimeException("Cannot modify already executed chain");
        }
        this.chain.tasks.add(new ChainTask(NOOP_RUNNABLE, duration, this::lastAsync, false));
    }
}
