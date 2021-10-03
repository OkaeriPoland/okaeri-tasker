package eu.okaeri.tasker.core;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class Tasker {

    public static Tasker newPool(TaskerExecutor executor) {
        return new Tasker(executor);
    }

    private final TaskerExecutor executor;

    public TaskerChain<Object> newChain() {
        return new TaskerChain<>(this.executor);
    }

    public TaskerChain<Object> newSharedChain(String name) {
        throw new RuntimeException("Not implemented yet");
    }
}
