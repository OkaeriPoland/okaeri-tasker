package eu.okaeri.core;

public final class Tasker {

    public static TaskerPool newPool(TaskerExecutor executor) {
        return new TaskerPool(executor);
    }
}
