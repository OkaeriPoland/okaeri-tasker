package eu.okaeri.tasker.core.context;

import lombok.NonNull;

public interface TaskerPlatform {
    TaskerContext getDefaultContext();
    void cancel(@NonNull Object task);
}
