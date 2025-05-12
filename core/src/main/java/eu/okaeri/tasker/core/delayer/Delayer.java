package eu.okaeri.tasker.core.delayer;

import eu.okaeri.tasker.core.context.TaskerContext;
import eu.okaeri.tasker.core.context.TaskerPlatform;
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

    private static final Runnable NOOP_RUNNABLE = () -> {
    };

    protected final AtomicReference<Object> task = new AtomicReference<>(null);
    protected final AtomicReference<Instant> started = new AtomicReference<>(null);
    protected final AtomicBoolean abort = new AtomicBoolean(false);

    protected final List<Supplier<Boolean>> abortWhen = new ArrayList<>();
    protected final List<Supplier<Boolean>> forceWhen = new ArrayList<>();
    protected final List<Runnable> actions = new ArrayList<>();
    protected final List<Runnable> forcedActions = new ArrayList<>();

    protected final TaskerPlatform platform;
    protected final Duration duration;
    protected final Duration checkRate;

    public static Delayer of(@NonNull TaskerPlatform platform, @NonNull Duration duration) {
        return of(platform, duration, duration.dividedBy(10));
    }

    public static Delayer of(@NonNull TaskerPlatform platform, @NonNull Duration duration, @NonNull Duration checkRate) {
        return new Delayer(platform, duration, checkRate);
    }

    public Delayer abortIf(@NonNull Supplier<Boolean> supplier) {
        this.abortWhen.add(supplier);
        return this;
    }

    public Delayer abortIfNot(@NonNull Supplier<Boolean> supplier) {
        return this.abortIf(() -> !supplier.get());
    }

    public Delayer abortIfThen(@NonNull Supplier<Boolean> supplier, @NonNull Runnable whenAbort) {
       return this.abortIfThenOrElse(supplier, whenAbort, NOOP_RUNNABLE);
    }

    public Delayer abortIfNotThen(@NonNull Supplier<Boolean> supplier, @NonNull Runnable whenAbort) {
        return this.abortIfThen(() -> !supplier.get(), whenAbort);
    }

    public Delayer abortIfOrElse(@NonNull Supplier<Boolean> supplier, @NonNull Runnable whenContinue) {
        return this.abortIfThenOrElse(supplier, NOOP_RUNNABLE, whenContinue);
    }

    public Delayer abortIfNotOrElse(@NonNull Supplier<Boolean> supplier, @NonNull Runnable whenContinue) {
        return this.abortIfOrElse(() -> !supplier.get(), whenContinue);
    }

    public Delayer abortIfThenOrElse(@NonNull Supplier<Boolean> supplier, @NonNull Runnable whenAbort, @NonNull Runnable whenContinue) {
        return this.abortIf(() -> {
            if (supplier.get()) {
                whenAbort.run();
                return true;
            }
            whenContinue.run();
            return false;
        });
    }

    public Delayer abortIfNotThenOrElse(@NonNull Supplier<Boolean> supplier, @NonNull Runnable whenAbort, @NonNull Runnable whenContinue) {
        return this.abortIfThenOrElse(() -> !supplier.get(), whenAbort, whenContinue);
    }

    public Delayer forceIf(@NonNull Supplier<Boolean> supplier) {
        this.forceWhen.add(supplier);
        return this;
    }

    public Delayer forceIfNot(@NonNull Supplier<Boolean> supplier) {
        return this.forceIf(() -> !supplier.get());
    }

    public Delayer forceIfThen(@NonNull Supplier<Boolean> supplier, @NonNull Runnable whenForce) {
        return this.forceIfThenOrElse(supplier, whenForce, NOOP_RUNNABLE);
    }

    public Delayer forceIfNotThen(@NonNull Supplier<Boolean> supplier, @NonNull Runnable whenForce) {
        return this.forceIfThen(() -> !supplier.get(), whenForce);
    }

    public Delayer forceIfOrElse(@NonNull Supplier<Boolean> supplier, @NonNull Runnable whenContinue) {
        return this.forceIfThenOrElse(supplier, NOOP_RUNNABLE, whenContinue);
    }

    public Delayer forceIfNotOrElse(@NonNull Supplier<Boolean> supplier, @NonNull Runnable whenContinue) {
        return this.forceIfOrElse(() -> !supplier.get(), whenContinue);
    }

    public Delayer forceIfThenOrElse(@NonNull Supplier<Boolean> supplier, @NonNull Runnable whenForce, @NonNull Runnable whenContinue) {
        return this.forceIf(() -> {
            if (supplier.get()) {
                whenForce.run();
                return true;
            }
            whenContinue.run();
            return false;
        });
    }

    public Delayer forceIfNotThenOrElse(@NonNull Supplier<Boolean> supplier, @NonNull Runnable whenForce, @NonNull Runnable whenContinue) {
        return this.forceIfThenOrElse(() -> !supplier.get(), whenForce, whenContinue);
    }

    public Delayer delayed(@NonNull Runnable action) {
        this.actions.add(action);
        return this;
    }

    public Delayer forced(@NonNull Runnable action) {
        this.forcedActions.add(action);
        return this;
    }

    public Delayer execute(@NonNull TaskerContext context) {

        if (!this.started.compareAndSet(null, Instant.now())) {
            throw new RuntimeException("Cannot execute already executed chain");
        }

        this.task.set(context.schedule(this::run, this.checkRate));
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

        // check force
        for (Supplier<Boolean> forceWhenSupplier : this.forceWhen) {

            try {
                if (!forceWhenSupplier.get()) {
                    continue;
                }
            } catch (Throwable throwable) {
                this.cancel();
                throw throwable;
            }

            this.cancel();
            this.forcedActions.forEach(Runnable::run);
            this.actions.forEach(Runnable::run);
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
        if (!this.abort.compareAndSet(false, true)) {
            return false; // already cancelled
        }
        this.platform.cancel(this.task.get());
        return true;
    }
}
