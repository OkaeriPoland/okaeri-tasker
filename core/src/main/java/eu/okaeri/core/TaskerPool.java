package eu.okaeri.core;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TaskerPool {

    private final TaskerExecutor executor;

    public TaskerChain<Object> newChain() {
        return new TaskerChain<>(this.executor);
    }
}
