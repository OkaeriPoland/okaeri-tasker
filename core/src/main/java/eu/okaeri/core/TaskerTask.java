package eu.okaeri.core;

import lombok.Data;
import lombok.experimental.Delegate;

@Data
class TaskerTask {
    @Delegate private final Runnable runnable;
    private final boolean async;
}