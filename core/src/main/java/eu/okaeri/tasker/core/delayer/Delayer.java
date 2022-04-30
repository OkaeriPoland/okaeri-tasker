package eu.okaeri.tasker.core.delayer;

import eu.okaeri.tasker.core.TaskerExecutor;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class Delayer {

    protected final AtomicReference<Object> task = new AtomicReference<>(null);
    protected final AtomicReference<Instant> started = new AtomicReference<>(null);
    protected final AtomicBoolean abort = new AtomicBoolean(false);

    protected final List<Supplier<Boolean>> abortWhen = new ArrayList<>();
    protected final List<Runnable> actions = new ArrayList<>();

    protected final TaskerExecutor<Object> executor;
    protected final Duration duration;
    protected final Duration checkRate;

    public static Delayer of(@NonNull TaskerExecutor<?> executor, @NonNull Duration duration) {
        return of(executor, duration, duration.dividedBy(10));
    }

    @SuppressWarnings("unchecked")
    public static Delayer of(@NonNull TaskerExecutor<?> executor, @NonNull Duration duration, @NonNull Duration checkRate) {
        return new Delayer((TaskerExecutor<Object>) executor, duration, checkRate);
    }

    public Delayer abortIf(@NonNull Supplier<Boolean> supplier) {
        this.abortWhen.add(supplier);
        return this;
    }

    public Delayer abortIfThen(@NonNull Supplier<Boolean> supplier, @NonNull Runnable runnable) {
        this.abortWhen.add(() -> {
            if (supplier.get()) {
                runnable.run();
                return true;
            }
            return false;
        });
        return this;
    }

    public Delayer delayed(@NonNull Runnable action) {
        this.actions.add(action);
        return this;
    }

    public Delayer executeSync() {
        return this.execute(false);
    }

    public Delayer executeAsync() {
        return this.execute(true);
    }

    public Delayer execute(boolean async) {

        if (this.started.get() != null) {
            throw new RuntimeException("Cannot execute already executed chain");
        }

        this.started.set(Instant.now());
        this.task.set(this.executor.schedule(this::run, this.checkRate, async));

        return this;
    }

    protected void run() {

        // already aborted
        if (this.abort.get()) {
            return;
        }

        // check aborts
        for (Supplier<Boolean> abortWhenSupplier : this.abortWhen) {

            try {
                if (!abortWhenSupplier.get()) {
                    continue;
                }
            } catch (Throwable throwable) {
                this.cancel();
                throw throwable;
            }

            this.cancel();
            return;
        }

        // still not done waiting
        if (Duration.between(this.started.get(), Instant.now()).compareTo(this.duration) < 0) {
            return;
        }

        // duration has passed with no abort
        this.cancel();
        this.actions.forEach(Runnable::run);
    }

    public boolean cancel() {

        if (this.abort.get()) {
            return false;
        }

        this.abort.set(true);
        this.executor.cancel(this.task.get());
        return true;
    }
}
