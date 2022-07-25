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

    private static final Runnable NOOP_RUNNABLE = () -> {
    };

    protected final AtomicBoolean abort = new AtomicBoolean(false);
    protected final AtomicBoolean lastAsync = new AtomicBoolean(false);
    protected final AtomicBoolean executed = new AtomicBoolean(false);
    protected final AtomicBoolean done = new AtomicBoolean(false);
    protected final AtomicBoolean cancelled = new AtomicBoolean(false);

    protected final AtomicReference<Object> data = new AtomicReference<>();
    protected final AtomicReference<Exception> exception = new AtomicReference<>();
    protected final AtomicReference<Exception> trace = new AtomicReference<>();
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
    protected TaskerChain<T> _abortIf(@NonNull Predicate<T> predicate, boolean async) {
        Runnable runnable = () -> {
            if (predicate.test((T) this.data.get())) {
                this.abort.set(true);
            }
        };
        return this.lastAsync.get() ? this.async(runnable) : this.sync(runnable);
    }

    public TaskerChain<T> abortIf(@NonNull Predicate<T> predicate) {
        return this._abortIf(predicate, this.lastAsync.get());
    }

    public TaskerChain<T> abortIfSync(@NonNull Predicate<T> predicate) {
        return this._abortIf(predicate, false);
    }

    public TaskerChain<T> abortIfAsync(@NonNull Predicate<T> predicate) {
        return this._abortIf(predicate, true);
    }

    public TaskerChain<T> abortIfNot(@NonNull Predicate<T> predicate) {
        return this._abortIf(data -> !predicate.test(data), this.lastAsync.get());
    }

    public TaskerChain<T> abortIfSyncNot(@NonNull Predicate<T> predicate) {
        return this._abortIf(data -> !predicate.test(data), false);
    }

    public TaskerChain<T> abortIfAsyncNot(@NonNull Predicate<T> predicate) {
        return this._abortIf(data -> !predicate.test(data), true);
    }

    public TaskerChain<T> abortIfThen(@NonNull Predicate<T> predicate, @NonNull Runnable whenAbort) {
        return this.abortIfThenOrElse(predicate, whenAbort, NOOP_RUNNABLE);
    }

    public TaskerChain<T> abortIfSyncThen(@NonNull Predicate<T> predicate, @NonNull Runnable whenAbort) {
        return this.abortIfSyncThenOrElse(predicate, whenAbort, NOOP_RUNNABLE);
    }

    public TaskerChain<T> abortIfAsyncThen(@NonNull Predicate<T> predicate, @NonNull Runnable whenAbort) {
        return this.abortIfAsyncThenOrElse(predicate, whenAbort, NOOP_RUNNABLE);
    }

    public TaskerChain<T> abortIfNotThen(@NonNull Predicate<T> predicate, @NonNull Runnable whenAbort) {
        return this.abortIfThen(data -> !predicate.test(data), whenAbort);
    }

    public TaskerChain<T> abortIfSyncNotThen(@NonNull Predicate<T> predicate, @NonNull Runnable whenAbort) {
        return this.abortIfSyncThen(data -> !predicate.test(data), whenAbort);
    }

    public TaskerChain<T> abortIfAsyncNotThen(@NonNull Predicate<T> predicate, @NonNull Runnable whenAbort) {
        return this.abortIfAsyncThen(data -> !predicate.test(data), whenAbort);
    }

    public TaskerChain<T> abortIfOrElse(@NonNull Predicate<T> predicate, @NonNull Runnable whenContinue) {
        return this.abortIfThenOrElse(predicate, NOOP_RUNNABLE, whenContinue);
    }

    public TaskerChain<T> abortIfSyncOrElse(@NonNull Predicate<T> predicate, @NonNull Runnable whenContinue) {
        return this.abortIfSyncThenOrElse(predicate, NOOP_RUNNABLE, whenContinue);
    }

    public TaskerChain<T> abortIfAsyncOrElse(@NonNull Predicate<T> predicate, @NonNull Runnable whenContinue) {
        return this.abortIfAsyncThenOrElse(predicate, NOOP_RUNNABLE, whenContinue);
    }

    public TaskerChain<T> abortIfNotOrElse(@NonNull Predicate<T> predicate, @NonNull Runnable whenContinue) {
        return this.abortIfOrElse(data -> !predicate.test(data), whenContinue);
    }

    public TaskerChain<T> abortIfSyncNotOrElse(@NonNull Predicate<T> predicate, @NonNull Runnable whenContinue) {
        return this.abortIfSyncOrElse(data -> !predicate.test(data), whenContinue);
    }

    public TaskerChain<T> abortIfAsyncNotOrElse(@NonNull Predicate<T> predicate, @NonNull Runnable whenContinue) {
        return this.abortIfAsyncOrElse(data -> !predicate.test(data), whenContinue);
    }

    private TaskerChain<T> _abortIfThenOrElse(@NonNull Predicate<T> predicate, @NonNull Runnable whenAbort, @NonNull Runnable whenContinue, boolean async) {
        return this._abortIf(data -> {
            if (predicate.test(data)) {
                whenAbort.run();
                return true;
            }
            whenContinue.run();
            return false;
        }, async);
    }

    public TaskerChain<T> abortIfThenOrElse(@NonNull Predicate<T> predicate, @NonNull Runnable whenAbort, @NonNull Runnable whenContinue) {
        return this._abortIfThenOrElse(predicate, whenAbort, whenContinue, this.lastAsync.get());
    }

    public TaskerChain<T> abortIfSyncThenOrElse(@NonNull Predicate<T> predicate, @NonNull Runnable whenAbort, @NonNull Runnable whenContinue) {
        return this._abortIfThenOrElse(predicate, whenAbort, whenContinue, false);
    }

    public TaskerChain<T> abortIfAsyncThenOrElse(@NonNull Predicate<T> predicate, @NonNull Runnable whenAbort, @NonNull Runnable whenContinue) {
        return this._abortIfThenOrElse(predicate, whenAbort, whenContinue, true);
    }

    public TaskerChain<T> abortIfNotThenOrElse(@NonNull Predicate<T> predicate, @NonNull Runnable whenAbort, @NonNull Runnable whenContinue) {
        return this.abortIfThenOrElse(data -> !predicate.test(data), whenAbort, whenContinue);
    }

    public TaskerChain<T> abortIfSyncNotThenOrElse(@NonNull Predicate<T> predicate, @NonNull Runnable whenAbort, @NonNull Runnable whenContinue) {
        return this.abortIfSyncThenOrElse(data -> !predicate.test(data), whenAbort, whenContinue);
    }

    public TaskerChain<T> abortIfAsyncNotThenOrElse(@NonNull Predicate<T> predicate, @NonNull Runnable whenAbort, @NonNull Runnable whenContinue) {
        return this.abortIfAsyncThenOrElse(data -> !predicate.test(data), whenAbort, whenContinue);
    }

    public TaskerChain<T> abortIf(@NonNull BooleanSupplier supplier) {
        return this._abortIf(unused -> supplier.getAsBoolean(), this.lastAsync.get());
    }

    public TaskerChain<T> abortIfSync(@NonNull BooleanSupplier supplier) {
        return this._abortIf(unused -> supplier.getAsBoolean(), false);
    }

    public TaskerChain<T> abortIfAsync(@NonNull BooleanSupplier supplier) {
        return this._abortIf(unused -> supplier.getAsBoolean(), true);
    }

    public TaskerChain<T> abortIfNot(@NonNull BooleanSupplier supplier) {
        return this._abortIf(unused -> !supplier.getAsBoolean(), this.lastAsync.get());
    }

    public TaskerChain<T> abortIfSyncNot(@NonNull BooleanSupplier supplier) {
        return this._abortIf(unused -> !supplier.getAsBoolean(), false);
    }

    public TaskerChain<T> abortIfAsyncNot(@NonNull BooleanSupplier supplier) {
        return this._abortIf(unused -> !supplier.getAsBoolean(), true);
    }

    public TaskerChain<T> abortIfThen(@NonNull BooleanSupplier supplier, @NonNull Runnable whenAbort) {
        return this.abortIfThenOrElse(unused -> supplier.getAsBoolean(), whenAbort, NOOP_RUNNABLE);
    }

    public TaskerChain<T> abortIfSyncThen(@NonNull BooleanSupplier supplier, @NonNull Runnable whenAbort) {
        return this.abortIfSyncThenOrElse(unused -> supplier.getAsBoolean(), whenAbort, NOOP_RUNNABLE);
    }

    public TaskerChain<T> abortIfAsyncThen(@NonNull BooleanSupplier supplier, @NonNull Runnable whenAbort) {
        return this.abortIfAsyncThenOrElse(unused -> supplier.getAsBoolean(), whenAbort, NOOP_RUNNABLE);
    }

    public TaskerChain<T> abortIfNotThen(@NonNull BooleanSupplier supplier, @NonNull Runnable whenAbort) {
        return this.abortIfThen(unused -> !supplier.getAsBoolean(), whenAbort);
    }

    public TaskerChain<T> abortIfSyncNotThen(@NonNull BooleanSupplier supplier, @NonNull Runnable whenAbort) {
        return this.abortIfSyncThen(unused -> !supplier.getAsBoolean(), whenAbort);
    }

    public TaskerChain<T> abortIfAsyncNotThen(@NonNull BooleanSupplier supplier, @NonNull Runnable whenAbort) {
        return this.abortIfAsyncThen(unused -> !supplier.getAsBoolean(), whenAbort);
    }

    public TaskerChain<T> abortIfOrElse(@NonNull BooleanSupplier supplier, @NonNull Runnable whenContinue) {
        return this.abortIfThenOrElse(unused -> supplier.getAsBoolean(), NOOP_RUNNABLE, whenContinue);
    }

    public TaskerChain<T> abortIfSyncOrElse(@NonNull BooleanSupplier supplier, @NonNull Runnable whenContinue) {
        return this.abortIfSyncThenOrElse(unused -> supplier.getAsBoolean(), NOOP_RUNNABLE, whenContinue);
    }

    public TaskerChain<T> abortIfAsyncOrElse(@NonNull BooleanSupplier supplier, @NonNull Runnable whenContinue) {
        return this.abortIfAsyncThenOrElse(unused -> supplier.getAsBoolean(), NOOP_RUNNABLE, whenContinue);
    }

    public TaskerChain<T> abortIfNotOrElse(@NonNull BooleanSupplier supplier, @NonNull Runnable whenContinue) {
        return this.abortIfOrElse(unused -> !supplier.getAsBoolean(), whenContinue);
    }

    public TaskerChain<T> abortIfSyncNotOrElse(@NonNull BooleanSupplier supplier, @NonNull Runnable whenContinue) {
        return this.abortIfSyncOrElse(unused -> !supplier.getAsBoolean(), whenContinue);
    }

    public TaskerChain<T> abortIfAsyncNotOrElse(@NonNull BooleanSupplier supplier, @NonNull Runnable whenContinue) {
        return this.abortIfAsyncOrElse(unused -> !supplier.getAsBoolean(), whenContinue);
    }

    public TaskerChain<T> abortIfThenOrElse(@NonNull BooleanSupplier supplier, @NonNull Runnable whenAbort, @NonNull Runnable whenContinue) {
        return this.abortIfThenOrElse(unused -> supplier.getAsBoolean(), whenAbort, whenContinue);
    }

    public TaskerChain<T> abortIfSyncThenOrElse(@NonNull BooleanSupplier supplier, @NonNull Runnable whenAbort, @NonNull Runnable whenContinue) {
        return this.abortIfSyncThenOrElse(unused -> supplier.getAsBoolean(), whenAbort, whenContinue);
    }

    public TaskerChain<T> abortIfAsyncThenOrElse(@NonNull BooleanSupplier supplier, @NonNull Runnable whenAbort, @NonNull Runnable whenContinue) {
        return this.abortIfAsyncThenOrElse(unused -> supplier.getAsBoolean(), whenAbort, whenContinue);
    }

    public TaskerChain<T> abortIfNotThenOrElse(@NonNull BooleanSupplier supplier, @NonNull Runnable whenAbort, @NonNull Runnable whenContinue) {
        return this.abortIfThenOrElse(unused -> !supplier.getAsBoolean(), whenAbort, whenContinue);
    }

    public TaskerChain<T> abortIfSyncNotThenOrElse(@NonNull BooleanSupplier supplier, @NonNull Runnable whenAbort, @NonNull Runnable whenContinue) {
        return this.abortIfSyncThenOrElse(unused -> !supplier.getAsBoolean(), whenAbort, whenContinue);
    }

    public TaskerChain<T> abortIfAsyncNotThenOrElse(@NonNull BooleanSupplier supplier, @NonNull Runnable whenAbort, @NonNull Runnable whenContinue) {
        return this.abortIfAsyncThenOrElse(unused -> !supplier.getAsBoolean(), whenAbort, whenContinue);
    }

    public TaskerChain<T> abortIfNull() {
        return this.abortIf(Objects::isNull);
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

        // save start trace
        this.trace.set(new RuntimeException("Chain trace point"));

        // add callback as last
        Runnable abortCallback = () -> {

            // handle exception after last task
            Exception unhandled = this.exception.get();
            if (unhandled != null) {
                if (unhandledExceptionConsumer != null) {
                    unhandledExceptionConsumer.accept(unhandled);
                } else {
                    Throwable throwable = this.trace.get().initCause(unhandled);
                    throw new RuntimeException("Unhandled chain exception", throwable);
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
                Throwable throwable = this.trace.get().initCause(unhandled);
                throw new RuntimeException("Unhandled chain exception", throwable);
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
