package eu.okaeri.tasker.core.chain;

import lombok.Data;
import lombok.experimental.Delegate;

import java.time.Duration;
import java.util.function.Supplier;

@Data
class ChainTask {
    @Delegate protected final Runnable runnable;
    protected final Duration delay;
    protected final Supplier<Boolean> async;
    protected final boolean exceptionHandler;
}
