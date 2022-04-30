package eu.okaeri.tasker.core.chain;

import lombok.Data;
import lombok.experimental.Delegate;

@Data
class ChainTask {
    @Delegate protected final Runnable runnable;
    protected final boolean async;
    protected final boolean exceptionHandler;
}
