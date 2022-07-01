package eu.okaeri.tasker.core.chain;

import eu.okaeri.tasker.core.TaskerExecutor;
import eu.okaeri.tasker.core.TaskerFuture;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;

public class TaskerChain<T> {

    protected final AtomicBoolean abort = new AtomicBoolean(false);
    protected final AtomicBoolean lastAsync = new AtomicBoolean(false);
    protected final AtomicBoolean executed = new AtomicBoolean(false);
    protected final AtomicBoolean done = new AtomicBoolean(false);
    protected final AtomicBoolean cancelled = new AtomicBoolean(false);

    protected final AtomicReference<Object> data = new AtomicReference<>();
    protected final AtomicReference<Exception> exception = new AtomicReference<>();
    protected final AtomicReference<Object> currentTask = new AtomicReference<>();

    protected final List<ChainTask> tasks = new ArrayList<>();
    protected final TaskerExecutor<Object> executor;

    @SuppressWarnings("unchecked")
    public TaskerChain(@NonNull TaskerExecutor<?> executor) {
        this.executor = (TaskerExecutor<Object>) executor;
    }

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
    protected TaskerChain<T> _abortIf(@NonNull Predicate<T> predicate, boolean async, Runnable abortRunnable) {
        Runnable runnable = () -> {
            if (predicate.test((T) this.data.get())) {
                this.abort.set(true);

                if (abortRunnable != null) {
                    if (async) {
                        this.async(abortRunnable);
                    } else {
                        this.sync(abortRunnable);
                    }
                }
            }
        };
        return async ? this.async(runnable) : this.sync(runnable);
    }

    public TaskerChain<T> abortIfSync(@NonNull Predicate<T> predicate, Runnable abortRunnable) {
        return this._abortIf(predicate, false, abortRunnable);
    }

    public TaskerChain<T> abortIfSync(@NonNull Predicate<T> predicate) {
        return this.abortIfSync(predicate, null);
    }

    public TaskerChain<T> abortIfAsync(@NonNull Predicate<T> predicate, Runnable abortRunnable) {
        return this._abortIf(predicate, true, abortRunnable);
    }

    public TaskerChain<T> abortIfAsync(@NonNull Predicate<T> predicate) {
        return this.abortIfAsync(predicate, null);
    }

    public TaskerChain<T> abortIf(@NonNull Predicate<T> predicate, Runnable abortRunnable) {
        return this._abortIf(predicate, this.lastAsync.get(), abortRunnable);
    }

    public TaskerChain<T> abortIf(@NonNull Predicate<T> predicate) {
        return this.abortIf(predicate, null);
    }

    public TaskerChain<T> abortIfSync(@NonNull BooleanSupplier supplier, Runnable abortRunnable) {
        return this._abortIf((unused) -> supplier.getAsBoolean(), false, abortRunnable);
    }

    public TaskerChain<T> abortIfSync(@NonNull BooleanSupplier supplier) {
        return this.abortIfSync(supplier, null);
    }

    public TaskerChain<T> abortIfAsync(@NonNull BooleanSupplier supplier, Runnable abortRunnable) {
        return this._abortIf((unused) -> supplier.getAsBoolean(), true, abortRunnable);
    }

    public TaskerChain<T> abortIfAsync(@NonNull BooleanSupplier supplier) {
        return this.abortIfAsync(supplier, null);
    }

    public TaskerChain<T> abortIf(@NonNull BooleanSupplier supplier, Runnable abortRunnable) {
        return this._abortIf((unused) -> supplier.getAsBoolean(), this.lastAsync.get(), abortRunnable);
    }

    public TaskerChain<T> abortIf(@NonNull BooleanSupplier supplier) {
        return this.abortIf(supplier, null);
    }

    public TaskerChain<T> abortIfNull(Runnable abortRunnable) {
        return this.abortIf(Objects::isNull, abortRunnable);
    }

    public TaskerChain<T> abortIfNull() {
        return this.abortIfNull(null);
    }

    // EXCEPTIONS
    @SuppressWarnings("unchecked")
    protected <E extends Exception> TaskerChain<T> _handleException(@NonNull Function<E, T> handler, boolean async) {
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
    protected void _execute(Consumer<T> consumer, Consumer<Exception> unhandledExceptionConsumer) {

        if (this.executed.get()) {
            throw new RuntimeException("Cannot execute already executed chain");
        }

        // add callback as last
        Runnable abortCallback = () -> {

            // handle exception after last task
            Exception unhandled = this.exception.get();
            if (unhandled != null) {
                if (unhandledExceptionConsumer != null) {
                    unhandledExceptionConsumer.accept(unhandled);
                } else {
                    throw new RuntimeException("Unhandled chain exception", unhandled);
                }
            }

            // callback consumer
            if (consumer != null) {
                consumer.accept((T) this.data.get());
            }

            // mark as done
            this.done.set(true);
        };

        // run tasks
        this._executeTask(0, abortCallback, unhandledExceptionConsumer);
    }

    protected void _executeTask(int index, Runnable abortCallback, Consumer<Exception> unhandledExceptionConsumer) {

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
            if (unhandledExceptionConsumer != null) {
                // pass exception to the consumer
                unhandledExceptionConsumer.accept(unhandled);
                // check if marked for abort
                if (this.abort.get()) {
                    abortCallback.run();
                    return;
                }
            } else {
                throw new RuntimeException("Unhandled chain exception", unhandled);
            }
        }

        // prepare callback
        Runnable callback = () -> {
            this.lastAsync.set(task.isAsync());
            this._executeTask(index + 1, abortCallback, unhandledExceptionConsumer);
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
        this.currentTask.set(this.executor.run(runnable, callback, task.isAsync()));
    }

    public void execute(@NonNull Consumer<T> consumer) {
        this._execute(consumer, null);
    }

    public void execute() {
        this._execute(null, null);
    }

    public Future<T> executeFuture() {
        return new TaskerFuture<>(this);
    }

    public T await() {
        return this.await(-1, null);
    }

    @SneakyThrows
    @SuppressWarnings("BusyWait")
    public T await(long timeout, TimeUnit unit) {

        Instant start = unit == null ? null : Instant.now();
        AtomicReference<T> resource = new AtomicReference<>();
        AtomicReference<Exception> exception = new AtomicReference<>();

        this._execute(
            resource::set,
            (unhandledException) -> {
                this.abort.set(true);
                this.cancelled.set(true);
                exception.set(unhandledException);
            }
        );

        while (!this.isDone()) {
            if (this.isCancelled()) {
                throw new TimeoutException("Task was cancelled");
            }
            if (unit != null) {
                Duration waitDuration = Duration.between(start, Instant.now());
                if (waitDuration.toNanos() >= unit.toNanos(timeout)) {
                    this.cancel();
                    throw new TimeoutException("No result after " + waitDuration);
                }
            }
            Thread.sleep(1L);
        }

        Exception unhandledException = exception.get();
        if (unhandledException != null) {
            throw unhandledException;
        }

        return resource.get();
    }

    public boolean cancel() {

        if (this.abort.get() || this.isCancelled()) {
            return false;
        }

        this.abort.set(true);
        this.cancelled.set(true);

        Object currentTask = this.currentTask.get();
        if (currentTask != null) {
            this.executor.cancel(currentTask);
        }

        return true;
    }

    public boolean isDone() {
        return this.done.get();
    }

    public boolean isCancelled() {
        return this.cancelled.get();
    }
}
