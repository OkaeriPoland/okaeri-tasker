package eu.okaeri.tasker.core.chain;

import lombok.Data;
import lombok.experimental.Delegate;

@Data
class ChainTask {
    @Delegate private final Runnable runnable;
    private final boolean async;
    private final boolean exceptionHandler;
}