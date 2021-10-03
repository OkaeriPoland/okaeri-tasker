package eu.okaeri.core;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class TaskerChain {

    private final AtomicReference<Object> data = new AtomicReference<>();
    private final AtomicReference<Boolean> abort = new AtomicReference<>(false);
    private final AtomicReference<Boolean> lastAsync = new AtomicReference<>(false);
    private final AtomicReference<Boolean> executed = new AtomicReference<>(false);
    private final List<TaskerTask> tasks = new ArrayList<>();
    private final TaskerExecutor executor;

    // SYNC
    public TaskerChain sync(@NonNull Runnable runnable) {
        if (this.executed.get()) {
            throw new RuntimeException("Cannot modify already executed chain");
        }
        this.tasks.add(new TaskerTask(runnable, false));
        return this;
    }

    public <T> TaskerChain sync(@NonNull Supplier<T> supplier) {
        return this.sync(() -> this.data.set(supplier.get()));
    }

    @SuppressWarnings("unchecked")
    public <T> TaskerChain acceptSync(@NonNull Consumer<T> data) {
        return this.sync(() -> data.accept((T) this.data.get()));
    }

    @SuppressWarnings("unchecked")
    public <T, R> TaskerChain acceptSync(@NonNull Function<T, R> function) {
        return this.sync(() -> function.apply((T) this.data.get()));
    }

    // ASYNC
    public TaskerChain async(@NonNull Runnable runnable) {
        if (this.executed.get()) {
            throw new RuntimeException("Cannot modify already executed chain");
        }
        this.tasks.add(new TaskerTask(runnable, true));
        return this;
    }

    public <T> TaskerChain async(@NonNull Supplier<T> supplier) {
        return this.async(() -> this.data.set(supplier.get()));
    }

    @SuppressWarnings("unchecked")
    public <T> TaskerChain acceptAsync(@NonNull Consumer<T> data) {
        return this.async(() -> data.accept((T) this.data.get()));
    }

    @SuppressWarnings("unchecked")
    public <T, R> TaskerChain acceptAsync(@NonNull Function<T, R> function) {
        return this.async(() -> function.apply((T) this.data.get()));
    }

    // UTILITY
    @SuppressWarnings("unchecked")
    public <T> TaskerChain abortIf(@NonNull Predicate<T> predicate) {
        Runnable runnable = () -> {
            if (predicate.test((T) this.data.get())) {
                this.abort.set(true);
            }
        };
        return this.lastAsync.get() ? this.async(runnable) : this.sync(runnable);
    }

    public TaskerChain abortIfNull() {
        return this.abortIf(Objects::isNull);
    }

    // EXECUTION
    @SuppressWarnings("unchecked")
    private <T> void _execute(Consumer<T> consumer) {
        if (this.executed.get()) {
            throw new RuntimeException("Cannot execute already executed chain");
        }
        // run tasks
        this._executeTask(0);
        // callback consumer
        if (consumer != null) {
            consumer.accept((T) this.data.get());
        }
    }

    private void _executeTask(int index) {

        // no more tasks
        if (index >= this.tasks.size()) {
            return;
        }

        // abort!
        if (this.abort.get()) {
            return;
        }

        // get task & prepare callback
        TaskerTask task = this.tasks.get(index);
        Runnable callback = () -> {
            this.lastAsync.set(task.isAsync());
            this._executeTask(index + 1);
        };

        // execute
        if (task.isAsync()) {
            this.executor.runAsync(task.getRunnable(), callback);
        } else {
            this.executor.runSync(task.getRunnable(), callback);
        }
    }

    public <T> void execute(@NonNull Consumer<T> consumer) {
        this._execute(consumer);
    }

    public void execute() {
        this._execute(null);
    }

    @SuppressWarnings("unchecked")
    public <T> T await() {
        this._execute(null);
        return (T) this.data.get();
    }
}
