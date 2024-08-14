package eu.okaeri.tasker.core.chain;

import eu.okaeri.tasker.core.TaskerFuture;
import eu.okaeri.tasker.core.Taskerable;
import eu.okaeri.tasker.core.TaskerableWrapper;
import eu.okaeri.tasker.core.context.DefaultTaskerContext;
import eu.okaeri.tasker.core.context.TaskerContext;
import eu.okaeri.tasker.core.context.TaskerPlatform;
import eu.okaeri.tasker.core.role.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import static eu.okaeri.tasker.core.TaskerDsl.*;
import static eu.okaeri.tasker.core.chain.TaskerChainAccessor.DATA_EXCEPTION;

@RequiredArgsConstructor
public class TaskerChain<T> {

    protected final TaskerPlatform platform;

    protected final List<ChainTask> tasks = new ArrayList<>();
    protected final TaskerChainAccessor accessor = new TaskerChainAccessor(this);
    protected volatile TaskerContext lastContext;

    protected volatile boolean abort = false;
    protected volatile boolean executed = false;
    protected volatile @Getter boolean done = false;
    protected volatile @Getter boolean cancelled = false;

    protected volatile Exception trace = null;
    protected volatile Object currentTask = null;
    protected volatile int currentTaskIndex = 0;

    protected void add(@NonNull ChainTask task) {
        if (this.executed) {
            throw new RuntimeException("Cannot modify already executed chain");
        }
        this.tasks.add(task);
    }

    public TaskerChain<T> delay(@NonNull Duration duration) {
        this.add(ChainTask.builder().delay(duration).build());
        return this;
    }

    @SuppressWarnings("unchecked")
    public <N> TaskerChain<N> nextIf(@NonNull Function<TaskerChainAccessor, Boolean> when, @NonNull Taskerable<N> taskerable) {
        this.add(ChainTask.builder().taskerable(taskerable).condition(when).build());
        return (TaskerChain<N>) this;
    }

    public <N> TaskerChain<N> nextIf(@NonNull Function<TaskerChainAccessor, Boolean> when, @NonNull TaskerFunction<T, N> function) {
        return this.nextIf(when, ((Taskerable<N>) function));
    }

    public <N> TaskerChain<N> next(@NonNull Taskerable<N> taskerable) {
        return this.nextIf(accessor -> true, taskerable);
    }

    public TaskerChain<T> next(@NonNull TaskerConsumer<T> consumer) {
        return this.next((Taskerable<T>) consumer);
    }

    public <N> TaskerChain<N> next(@NonNull TaskerFunction<T, N> function) {
        return this.next((Taskerable<N>) function);
    }

    public TaskerChain<T> next(@NonNull TaskerRunnable<T> runnable) {
        return this.next((Taskerable<T>) runnable);
    }

    public <N> TaskerChain<N> $if(@NonNull Function<TaskerChainAccessor, Boolean> when, @NonNull Taskerable<N> taskerable) {
        return this.nextIf(when, taskerable);
    }

    public <N> TaskerChain<N> $if(@NonNull Function<TaskerChainAccessor, Boolean> when, @NonNull TaskerFunction<T, N> function) {
        return this.nextIf(when, function);
    }

    public TaskerChain<T> $if(@NonNull Function<TaskerChainAccessor, Boolean> when, @NonNull TaskerRunnable<T> runnable) {
        return this.nextIf(when, runnable);
    }

    public <N> TaskerChain<N> $(@NonNull Taskerable<N> taskerable) {
        return this.next(taskerable);
    }

    public TaskerChain<T> $(@NonNull TaskerConsumer<T> function) {
        return this.next(function);
    }

    public <N> TaskerChain<N> $(@NonNull TaskerFunction<T, N> function) {
        return this.next(function);
    }

    public TaskerChain<T> $(@NonNull TaskerRunnable<T> runnable) {
        return this.next(runnable);
    }

    public TaskerChain<T> abortIf(@NonNull TaskerPredicate<T> predicate) {
        return this
            .next(predicate.output("willAbort"))
            .next(raw(accessor -> accessor.abort(accessor.data("willAbort"))));
    }

    public <N> TaskerChain<N> abortIfThen(@NonNull TaskerPredicate<T> predicate, @NonNull Taskerable<N> taskerable) {
        return this
            .next(predicate.output("willAbort"))
            .nextIf(accessor -> accessor.data("willAbort"), taskerable)
            .next(raw(accessor -> accessor.abort(accessor.data("willAbort"))));
    }

    public <N> TaskerChain<N> abortIfThen(@NonNull TaskerPredicate<T> predicate, @NonNull TaskerFunction<T, N> function) {
        return this.abortIfThen(predicate, (Taskerable<N>) function);
    }

    public TaskerChain<T> abortIfThen(@NonNull TaskerPredicate<T> predicate, @NonNull TaskerRunnable<T> runnable) {
        return this.abortIfThen(predicate, (Taskerable<T>) runnable);
    }

    public TaskerChain<T> abortIfNull() {
        return this.abortIf(cond(Objects::isNull));
    }

    public <N> TaskerChain<N> abortIfNullThen(@NonNull Taskerable<N> taskerable) {
        return this.abortIfThen(cond(Objects::isNull), taskerable);
    }

    public <N> TaskerChain<N> abortIfNullThen(@NonNull TaskerFunction<T, N> function) {
        return this.abortIfNullThen((Taskerable<N>) function);
    }

    public TaskerChain<T> abortIfNullThen(@NonNull TaskerRunnable<T> runnable) {
        return this.abortIfNullThen((Taskerable<T>) runnable);
    }

    public TaskerChain<T> abortIfException() {
        this.add(ChainTask.builder()
            .condition(accessor -> accessor.has(DATA_EXCEPTION))
            .taskerable(raw(accessor -> accessor.abort(accessor.remove(DATA_EXCEPTION) != null)))
            .exceptionHandler(true)
            .build());
        return this;
    }

    @SuppressWarnings("unchecked")
    public <N> TaskerChain<N> abortIfExceptionThen(@NonNull Taskerable<N> taskerable) {
        this.nextIf(accessor -> accessor.has(DATA_EXCEPTION), taskerable);
        this.add(ChainTask.builder()
            .condition(accessor -> accessor.has(DATA_EXCEPTION))
            .taskerable(raw(accessor -> accessor.abort(accessor.remove(DATA_EXCEPTION) != null)))
            .exceptionHandler(true)
            .build());
        return (TaskerChain<N>) this;
    }

    public <N> TaskerChain<N> abortIfExceptionThen(@NonNull TaskerFunction<T, N> function) {
        return this.abortIfExceptionThen(((Taskerable<N>) function));
    }

    public TaskerChain<T> abortIfExceptionThen(@NonNull TaskerRunnable<T> runnable) {
        return this.abortIfExceptionThen(((Taskerable<T>) runnable));
    }

    @SuppressWarnings("unchecked")
    public <N> TaskerChain<N> handleException(@NonNull Taskerable<N> taskerable) {
        this.add(ChainTask.builder()
            .condition(accessor -> accessor.has(DATA_EXCEPTION))
            .taskerable(new TaskerableWrapper<N>(taskerable) {
                @Override
                public void call(@NonNull TaskerChainAccessor accessor, @NonNull Runnable callback) {
                    accessor.remove(DATA_EXCEPTION);
                    super.call(accessor, callback);
                }
            })
            .exceptionHandler(true)
            .build());
        return (TaskerChain<N>) this;
    }

    public <E extends Throwable, N> TaskerChain<N> handleException(@NonNull TaskerFunction<E, N> function) {
        return this.handleException((Taskerable<N>) function);
    }

    // EXECUTION
    protected void _execute(Consumer<T> consumer, Consumer<Exception> unhandledExceptionConsumer) {

        if (this.executed) {
            throw new RuntimeException("Cannot execute already executed chain");
        }

        // save start trace
        this.trace = new RuntimeException("Chain trace point");

        // add callback as last
        Runnable abortCallback = () -> {

            // handle exception after last task
            Exception unhandled = this.accessor.data(DATA_EXCEPTION);
            if (unhandled != null) {
                if (unhandledExceptionConsumer != null) {
                    unhandledExceptionConsumer.accept(unhandled);
                } else {
                    Throwable throwable = this.trace.initCause(unhandled);
                    throw new RuntimeException("Unhandled chain exception", throwable);
                }
            }

            // callback consumer
            if (consumer != null) {
                consumer.accept(this.accessor.data());
            }

            // mark as done
            this.done = true;
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
        if (this.abort) {
            abortCallback.run();
            return;
        }

        // get task
        ChainTask task = this.tasks.get(index);

        // check for unhandled exceptions
        Exception unhandled = this.accessor.data(DATA_EXCEPTION);
        if (unhandled != null && !task.isExceptionHandler()) {
            if (unhandledExceptionConsumer != null) {
                // pass exception to the consumer
                unhandledExceptionConsumer.accept(unhandled);
                // check if marked for abort
                if (this.abort) {
                    abortCallback.run();
                    return;
                }
            } else {
                Throwable throwable = this.trace.initCause(unhandled);
                throw new RuntimeException("Unhandled chain exception", throwable);
            }
        }

        // determine context
        TaskerContext context = (task.context() instanceof DefaultTaskerContext)
            ? this.platform.getDefaultContext() // try platform provided default
            : task.context();

        // prepare callback
        Runnable callback = () -> {
            this.lastContext = context;
            this._executeTask(index + 1, abortCallback, unhandledExceptionConsumer);
        };

        // create handling runnable
        Runnable runnable = () -> {
            if (!task.condition.apply(this.accessor)) {
                callback.run();
                return;
            }
            try {
                task.call(this.accessor, callback);
            } catch (Throwable exception) {
                this.accessor.data(DATA_EXCEPTION, exception);
                callback.run();
            }
        };

        // execute
        this.currentTaskIndex = index;
        this.currentTask = context.runLater(task.getDelay(), runnable);
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
                this.abort = true;
                this.cancelled = true;
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

        if (this.abort || this.isCancelled()) {
            return false;
        }

        this.abort = true;
        this.cancelled = true;

        Object currentTask = this.currentTask;
        if (currentTask != null) {
            this.platform.cancel(currentTask);
        }

        return true;
    }
}
