package eu.okaeri.tasker.core.context;

import lombok.NonNull;

public class DefaultTaskerPlatform implements TaskerPlatform {

    @Override
    public TaskerContext getDefaultContext() {
        return DefaultTaskerContext.getInstance();
    }

    @Override
    public void cancel(@NonNull Object task) {

    }
}
