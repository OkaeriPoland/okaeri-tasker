package eu.okaeri.tasker.core;

import eu.okaeri.tasker.core.chain.SharedChain;
import eu.okaeri.tasker.core.chain.TaskerChain;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class Tasker {

    private final Map<String, Queue<Runnable>> sharedChains = new ConcurrentHashMap<>();

    public static Tasker newPool(TaskerExecutor executor) {
        return new Tasker(executor);
    }

    private final TaskerExecutor executor;

    public TaskerChain<Object> newChain() {
        return new TaskerChain<>(this.executor);
    }

    public TaskerChain<Object> newSharedChain(String name) {
        Queue<Runnable> queue = this.getSharedChainQueue(name);
        return new SharedChain<>(this.executor, queue);
    }

    private Queue<Runnable> getSharedChainQueue(String name) {

        if (this.sharedChains.containsKey(name)) {
            return this.sharedChains.get(name);
        }

        Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
        this.sharedChains.put(name, queue);
        this.executor.schedule(() -> this.execSharedChainQueue(name), true);
        return queue;
    }

    private void execSharedChainQueue(String name) {

        Queue<Runnable> queue = this.sharedChains.get(name);
        if (queue == null) {
            return;
        }

        while (!queue.isEmpty()) {
            queue.poll().run();
        }
    }
}
