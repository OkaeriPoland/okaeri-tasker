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
public class TaskerChain<T> {

    private final AtomicReference<Object> data = new AtomicReference<>();
    private final AtomicReference<Boolean> abort = new AtomicReference<>(false);
    private final AtomicReference<Boolean> lastAsync = new AtomicReference<>(false);
    private final AtomicReference<Boolean> executed = new AtomicReference<>(false);
    private final AtomicReference<Exception> exception = new AtomicReference<>(null);

    private final List<TaskerTask> tasks = new ArrayList<>();
    private final TaskerExecutor executor;

    // SYNC
    public TaskerChain<T> sync(@NonNull Runnable runnable) {
        if (this.executed.get()) {
            throw new RuntimeException("Cannot modify already executed chain");
        }
        this.tasks.add(new TaskerTask(runnable, false, false));
        return this;
    }

    @SuppressWarnings("unchecked")
    public <N> TaskerChain<N> sync(@NonNull Supplier<N> supplier) {
        return (TaskerChain<N>) this.sync(() -> this.data.set(supplier.get()));
    }

    @SuppressWarnings("unchecked")
    public TaskerChain<T> acceptSync(@NonNull Consumer<T> data) {
        return this.sync(() -> data.accept((T) this.data.get()));
    }

    @SuppressWarnings("unchecked")
    public <R> TaskerChain<R> acceptSync(@NonNull Function<T, R> function) {
        return this.sync(() -> function.apply((T) this.data.get()));
    }

    // ASYNC
    public TaskerChain<T> async(@NonNull Runnable runnable) {
        if (this.executed.get()) {
            throw new RuntimeException("Cannot modify already executed chain");
        }
        this.tasks.add(new TaskerTask(runnable, true, false));
        return this;
    }

    @SuppressWarnings("unchecked")
    public <N> TaskerChain<N> async(@NonNull Supplier<N> supplier) {
        return (TaskerChain<N>) this.async(() -> this.data.set(supplier.get()));
    }

    @SuppressWarnings("unchecked")
    public TaskerChain<T> acceptAsync(@NonNull Consumer<T> data) {
        return this.async(() -> data.accept((T) this.data.get()));
    }

    @SuppressWarnings("unchecked")
    public <R> TaskerChain<R> acceptAsync(@NonNull Function<T, R> function) {
        return this.async(() -> function.apply((T) this.data.get()));
    }

    // UTILITY
    @SuppressWarnings("unchecked")
    public TaskerChain<T> abortIf(@NonNull Predicate<T> predicate) {
        Runnable runnable = () -> {
            if (predicate.test((T) this.data.get())) {
                this.abort.set(true);
            }
        };
        return this.lastAsync.get() ? this.async(runnable) : this.sync(runnable);
    }

    public TaskerChain<T> abortIfNull() {
        return this.abortIf(Objects::isNull);
    }

    // EXCEPTIONS
    private TaskerChain<T> _handleException(@NonNull Function<Exception, T> handler, boolean async) {
        if (this.executed.get()) {
            throw new RuntimeException("Cannot modify already executed chain");
        }
        Runnable task = () -> {
            Exception exception = this.exception.get();
            if (exception == null) {
                return;
            }
            this.data.set(handler.apply(exception));
            this.exception.set(null);
        };
        this.tasks.add(new TaskerTask(task, async, true));
        return this;
    }

    public TaskerChain<T> handleExceptionSync(@NonNull Function<Exception, T> handler) {
        return this._handleException(handler, false);
    }

    public TaskerChain<T> handleExceptionAsync(@NonNull Function<Exception, T> handler) {
        return this._handleException(handler, true);
    }

    public TaskerChain<T> abortIfException(@NonNull Consumer<Exception> handler) {
        return this._handleException((exception) -> {
            handler.accept(exception);
            this.abort.set(true);
            return null;
        }, this.lastAsync.get());
    }

    public TaskerChain<T> abortIfException() {
        return this.abortIfException(Function.identity()::apply);
    }

    // EXECUTION
    @SuppressWarnings("unchecked")
    private void _execute(Consumer<T> consumer) {

        if (this.executed.get()) {
            throw new RuntimeException("Cannot execute already executed chain");
        }

        // run tasks
        this._executeTask(0);

        // handle exception after last task
        Exception unhandled = this.exception.get();
        if (unhandled != null) {
            throw new RuntimeException("Unhandled chain exception", unhandled);
        }

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

        // get task
        TaskerTask task = this.tasks.get(index);

        // check for unhandled exceptions
        Exception unhandled = this.exception.get();
        if (unhandled != null && !task.isExceptionHandler()) {
            throw new RuntimeException("Unhandled chain exception", unhandled);
        }

        // prepare callback
        Runnable callback = () -> {
            this.lastAsync.set(task.isAsync());
            this._executeTask(index + 1);
        };

        // create handling runnable
        Runnable runnable = () -> {
            try {
                task.getRunnable().run();
            } catch (Exception exception) {
                this.exception.set(exception);
            }
        };

        // execute
        this.executor.run(runnable, callback, task.isAsync());
    }

    public void execute(@NonNull Consumer<T> consumer) {
        this._execute(consumer);
    }

    public void execute() {
        this._execute(null);
    }

    @SuppressWarnings("unchecked")
    public T await() {
        this._execute(null);
        return (T) this.data.get();
    }
}
