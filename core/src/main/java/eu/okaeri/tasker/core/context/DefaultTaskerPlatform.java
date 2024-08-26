package eu.okaeri.tasker.core.context;

import lombok.NonNull;
import lombok.SneakyThrows;

public class DefaultTaskerPlatform implements TaskerPlatform {

    @Override
    public TaskerContext getDefaultContext() {
        return DefaultTaskerContext.getInstance();
    }

    @Override
    @SneakyThrows
    public void cancel(@NonNull Object task) {
        if (task instanceof DefaultTaskerContext.CloseableThread) {
            ((DefaultTaskerContext.CloseableThread) task).close();
        } else {
            ((Thread) task).interrupt();
        }
    }
}
