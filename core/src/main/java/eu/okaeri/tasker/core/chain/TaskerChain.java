package eu.okaeri.tasker.core.chain;

import eu.okaeri.tasker.core.TaskerExecutor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class TaskerChain<T> {

    private final AtomicBoolean abort = new AtomicBoolean(false);
    private final AtomicBoolean lastAsync = new AtomicBoolean(false);
    private final AtomicBoolean executed = new AtomicBoolean(false);

    private final AtomicReference<Object> data = new AtomicReference<>();
    private final AtomicReference<Exception> exception = new AtomicReference<>();

    private final List<ChainTask> tasks = new ArrayList<>();
    private final TaskerExecutor executor;

    // SYNC
    public TaskerChain<T> sync(@NonNull Runnable runnable) {
        if (this.executed.get()) {
            throw new RuntimeException("Cannot modify already executed chain");
        }
        this.tasks.add(new ChainTask(runnable, false, false));
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
        this.tasks.add(new ChainTask(runnable, true, false));
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
    private TaskerChain<T> _abortIf(@NonNull Predicate<T> predicate, boolean async) {
        Runnable runnable = () -> {
            if (predicate.test((T) this.data.get())) {
                this.abort.set(true);
            }
        };
        return this.lastAsync.get() ? this.async(runnable) : this.sync(runnable);
    }

    public TaskerChain<T> abortIfSync(@NonNull Predicate<T> predicate) {
        return this._abortIf(predicate, false);
    }

    public TaskerChain<T> abortIfAsync(@NonNull Predicate<T> predicate) {
        return this._abortIf(predicate, true);
    }

    public TaskerChain<T> abortIf(@NonNull Predicate<T> predicate) {
        return this._abortIf(predicate, this.lastAsync.get());
    }

    public TaskerChain<T> abortIfNull() {
        return this.abortIf(Objects::isNull);
    }

    // EXCEPTIONS
    @SuppressWarnings("unchecked")
    private <E extends Exception> TaskerChain<T> _handleException(@NonNull Function<E, T> handler, boolean async) {
        if (this.executed.get()) {
            throw new RuntimeException("Cannot modify already executed chain");
        }
        Runnable task = () -> {
            Exception exception = this.exception.get();
            if (exception == null) {
                return;
            }
            this.data.set(handler.apply((E) exception));
            this.exception.set(null);
        };
        this.tasks.add(new ChainTask(task, async, true));
        return this;
    }

    public <E extends Exception> TaskerChain<T> handleExceptionSync(@NonNull Function<E, T> handler) {
        return this._handleException(handler, false);
    }

    public <E extends Exception> TaskerChain<T> handleExceptionAsync(@NonNull Function<E, T> handler) {
        return this._handleException(handler, true);
    }

    @SuppressWarnings("unchecked")
    public <E extends Exception> TaskerChain<T> abortIfException(@NonNull Consumer<E> handler) {
        return this._handleException((exception) -> {
            handler.accept((E) exception);
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

        // add callback as last
        Runnable abortCallback = () -> {

            // handle exception after last task
            Exception unhandled = this.exception.get();
            if (unhandled != null) {
                throw new RuntimeException("Unhandled chain exception", unhandled);
            }

            // callback consumer
            if (consumer != null) {
                consumer.accept((T) this.data.get());
            }
        };

        // run tasks
        this._executeTask(0, abortCallback);
    }

    private void _executeTask(int index, Runnable abortCallback) {

        // no more tasks
        if (index >= this.tasks.size()) {
            abortCallback.run();
            return;
        }

        // abort!
        if (this.abort.get()) {
            abortCallback.run();
            return;
        }

        // get task
        ChainTask task = this.tasks.get(index);

        // check for unhandled exceptions
        Exception unhandled = this.exception.get();
        if (unhandled != null && !task.isExceptionHandler()) {
            throw new RuntimeException("Unhandled chain exception", unhandled);
        }

        // prepare callback
        Runnable callback = () -> {
            this.lastAsync.set(task.isAsync());
            this._executeTask(index + 1, abortCallback);
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

    @SneakyThrows
    @SuppressWarnings("BusyWait")
    public T await() {

        AtomicBoolean done = new AtomicBoolean();
        AtomicReference<T> resource = new AtomicReference<>();

        this._execute((data) -> {
            resource.set(data);
            done.set(true);
        });

        while (!done.get()) {
            Thread.sleep(1L);
        }

        return resource.get();
    }
}
