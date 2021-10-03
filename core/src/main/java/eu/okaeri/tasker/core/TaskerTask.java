package eu.okaeri.tasker.core;

import lombok.Data;
import lombok.experimental.Delegate;

@Data
class TaskerTask {
    @Delegate private final Runnable runnable;
    private final boolean async;
    private final boolean exceptionHandler;
}