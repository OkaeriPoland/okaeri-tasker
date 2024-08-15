package eu.okaeri.tasker.bukkit.chain;

import eu.okaeri.tasker.bukkit.BukkitTasker;
import eu.okaeri.tasker.core.TaskerDsl;
import eu.okaeri.tasker.core.Taskerable;
import eu.okaeri.tasker.core.chain.TaskerChain;
import eu.okaeri.tasker.core.chain.TaskerChainAccessor;
import eu.okaeri.tasker.core.role.TaskerConsumer;
import eu.okaeri.tasker.core.role.TaskerFunction;
import eu.okaeri.tasker.core.role.TaskerPredicate;
import eu.okaeri.tasker.core.role.TaskerRunnable;
import lombok.NonNull;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class BukkitTaskerChain<T> extends TaskerChain<T> {

    public BukkitTaskerChain(BukkitTasker tasker) {
        super(tasker);
    }

    public <N> BukkitTaskerChain<N> syncIf(@NonNull Function<TaskerChainAccessor, Boolean> when, @NonNull Taskerable<N> taskerable) {
        return this.nextIf(when, taskerable.context(() -> ((BukkitTasker) this.tasker).getSync()));
    }

    public <N> BukkitTaskerChain<N> syncIf(@NonNull Function<TaskerChainAccessor, Boolean> when, @NonNull TaskerFunction<T, N> function) {
        return this.syncIf(when, ((Taskerable<N>) function));
    }

    public <N> BukkitTaskerChain<N> sync(@NonNull Taskerable<N> taskerable) {
        return this.syncIf(accessor -> true, taskerable);
    }

    public BukkitTaskerChain<T> sync(@NonNull TaskerConsumer<T> consumer) {
        return this.sync((Taskerable<T>) consumer);
    }

    public <N> BukkitTaskerChain<N> sync(@NonNull TaskerFunction<T, N> function) {
        return this.sync((Taskerable<N>) function);
    }

    public BukkitTaskerChain<T> sync(@NonNull TaskerRunnable<T> runnable) {
        return this.sync((Taskerable<T>) runnable);
    }


    public BukkitTaskerChain<T> accept(@NonNull Consumer<T> consumer) {
        return this.next(TaskerDsl.accept(consumer));
    }

    public <N> BukkitTaskerChain<N> transform(@NonNull Function<T, N> function) {
        return this.next(TaskerDsl.transform(function));
    }

    public BukkitTaskerChain<T> run(@NonNull Runnable runnable) {
        return this.next(TaskerDsl.run(runnable));
    }

    public <N> BukkitTaskerChain<N> supply(@NonNull Supplier<N> supplier) {
        return this.next(TaskerDsl.supply(supplier));
    }


    public BukkitTaskerChain<T> acceptSync(@NonNull Consumer<T> consumer) {
        return this.sync(TaskerDsl.accept(consumer));
    }

    public <N> BukkitTaskerChain<N> transformSync(@NonNull Function<T, N> function) {
        return this.sync(TaskerDsl.transform(function));
    }

    public BukkitTaskerChain<T> runSync(@NonNull Runnable runnable) {
        return this.sync(TaskerDsl.run(runnable));
    }

    public <N> BukkitTaskerChain<N> supplySync(@NonNull Supplier<N> supplier) {
        return this.sync(TaskerDsl.supply(supplier));
    }


    @Override
    public BukkitTaskerChain<T> delay(@NonNull Duration duration) {
        return (BukkitTaskerChain<T>) super.delay(duration);
    }

    @Override
    public <N> BukkitTaskerChain<N> nextIf(@NonNull Function<TaskerChainAccessor, Boolean> when, @NonNull Taskerable<N> taskerable) {
        return (BukkitTaskerChain<N>) super.nextIf(when, taskerable);
    }

    @Override
    public <N> BukkitTaskerChain<N> nextIf(@NonNull Function<TaskerChainAccessor, Boolean> when, @NonNull TaskerFunction<T, N> function) {
        return (BukkitTaskerChain<N>) super.nextIf(when, function);
    }

    @Override
    public <N> BukkitTaskerChain<N> next(@NonNull Taskerable<N> taskerable) {
        return (BukkitTaskerChain<N>) super.next(taskerable);
    }

    @Override
    public BukkitTaskerChain<T> next(@NonNull TaskerConsumer<T> consumer) {
        return (BukkitTaskerChain<T>) super.next(consumer);
    }

    @Override
    public <N> BukkitTaskerChain<N> next(@NonNull TaskerFunction<T, N> function) {
        return (BukkitTaskerChain<N>) super.next(function);
    }

    @Override
    public BukkitTaskerChain<T> next(@NonNull TaskerRunnable<T> runnable) {
        return (BukkitTaskerChain<T>) super.next(runnable);
    }

    @Override
    public <N> BukkitTaskerChain<N> $if(@NonNull Function<TaskerChainAccessor, Boolean> when, @NonNull Taskerable<N> taskerable) {
        return (BukkitTaskerChain<N>) super.$if(when, taskerable);
    }

    @Override
    public <N> BukkitTaskerChain<N> $if(@NonNull Function<TaskerChainAccessor, Boolean> when, @NonNull TaskerFunction<T, N> function) {
        return (BukkitTaskerChain<N>) super.$if(when, function);
    }

    @Override
    public BukkitTaskerChain<T> $if(@NonNull Function<TaskerChainAccessor, Boolean> when, @NonNull TaskerRunnable<T> runnable) {
        return (BukkitTaskerChain<T>) super.$if(when, runnable);
    }

    @Override
    public <N> BukkitTaskerChain<N> $(@NonNull Taskerable<N> taskerable) {
        return (BukkitTaskerChain<N>) super.$(taskerable);
    }

    @Override
    public BukkitTaskerChain<T> $(@NonNull TaskerConsumer<T> function) {
        return (BukkitTaskerChain<T>) super.$(function);
    }

    @Override
    public <N> BukkitTaskerChain<N> $(@NonNull TaskerFunction<T, N> function) {
        return (BukkitTaskerChain<N>) super.$(function);
    }

    @Override
    public BukkitTaskerChain<T> $(@NonNull TaskerRunnable<T> runnable) {
        return (BukkitTaskerChain<T>) super.$(runnable);
    }

    @Override
    public BukkitTaskerChain<T> abortIf(@NonNull TaskerPredicate<T> predicate) {
        return (BukkitTaskerChain<T>) super.abortIf(predicate);
    }

    @Override
    public <N> BukkitTaskerChain<N> abortIfThen(@NonNull TaskerPredicate<T> predicate, @NonNull Taskerable<N> taskerable) {
        return (BukkitTaskerChain<N>) super.abortIfThen(predicate, taskerable);
    }

    @Override
    public BukkitTaskerChain<T> abortIfThen(@NonNull TaskerPredicate<T> predicate, @NonNull TaskerConsumer<T> consumer) {
        return (BukkitTaskerChain<T>) super.abortIfThen(predicate, consumer);
    }

    @Override
    public <N> BukkitTaskerChain<N> abortIfThen(@NonNull TaskerPredicate<T> predicate, @NonNull TaskerFunction<T, N> function) {
        return (BukkitTaskerChain<N>) super.abortIfThen(predicate, function);
    }

    @Override
    public BukkitTaskerChain<T> abortIfThen(@NonNull TaskerPredicate<T> predicate, @NonNull TaskerRunnable<T> runnable) {
        return (BukkitTaskerChain<T>) super.abortIfThen(predicate, runnable);
    }

    @Override
    public BukkitTaskerChain<T> abortIfNull() {
        return (BukkitTaskerChain<T>) super.abortIfNull();
    }

    @Override
    public <N> BukkitTaskerChain<N> abortIfNullThen(@NonNull Taskerable<N> taskerable) {
        return (BukkitTaskerChain<N>) super.abortIfNullThen(taskerable);
    }

    @Override
    public BukkitTaskerChain<T> abortIfNullThen(@NonNull TaskerConsumer<T> consumer) {
        return (BukkitTaskerChain<T>) super.abortIfNullThen(consumer);
    }

    @Override
    public <N> BukkitTaskerChain<N> abortIfNullThen(@NonNull TaskerFunction<T, N> function) {
        return (BukkitTaskerChain<N>) super.abortIfNullThen(function);
    }

    @Override
    public BukkitTaskerChain<T> abortIfNullThen(@NonNull TaskerRunnable<T> runnable) {
        return (BukkitTaskerChain<T>) super.abortIfNullThen(runnable);
    }

    @Override
    public BukkitTaskerChain<T> abortIfException() {
        return (BukkitTaskerChain<T>) super.abortIfException();
    }

    @Override
    public <N> BukkitTaskerChain<N> abortIfExceptionThen(@NonNull Taskerable<N> taskerable) {
        return (BukkitTaskerChain<N>) super.abortIfExceptionThen(taskerable);
    }

    @Override
    public BukkitTaskerChain<T> abortIfExceptionThen(@NonNull TaskerConsumer<T> consumer) {
        return (BukkitTaskerChain<T>) super.abortIfExceptionThen(consumer);
    }

    @Override
    public <N> BukkitTaskerChain<N> abortIfExceptionThen(@NonNull TaskerFunction<T, N> function) {
        return (BukkitTaskerChain<N>) super.abortIfExceptionThen(function);
    }

    @Override
    public BukkitTaskerChain<T> abortIfExceptionThen(@NonNull TaskerRunnable<T> runnable) {
        return (BukkitTaskerChain<T>) super.abortIfExceptionThen(runnable);
    }

    @Override
    public <N> BukkitTaskerChain<N> handleException(@NonNull Taskerable<N> taskerable) {
        return (BukkitTaskerChain<N>) super.handleException(taskerable);
    }

    @Override
    public <E extends Throwable, N> BukkitTaskerChain<N> handleException(@NonNull TaskerFunction<E, N> function) {
        return (BukkitTaskerChain<N>) super.handleException(function);
    }
}
