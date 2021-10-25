package eu.okaeri.tasker.core;

import eu.okaeri.tasker.core.chain.SharedChain;
import eu.okaeri.tasker.core.chain.TaskerChain;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class Tasker {

    private final Map<String, Queue<Runnable>> sharedChains = new ConcurrentHashMap<>();
    private final Map<String, Queue<Runnable>> sharedChainsPriority = new ConcurrentHashMap<>();
    private final Map<String, Object> sharedChainsTasks = new ConcurrentHashMap<>();
    private final Map<String, AtomicBoolean> sharedChainsLocks = new ConcurrentHashMap<>();

    public static Tasker newPool(TaskerExecutor<?> executor) {
        return new Tasker(executor);
    }

    private final TaskerExecutor<?> executor;

    public TaskerChain<Object> newChain() {
        return new TaskerChain<>(this.executor);
    }

    public TaskerChain<Object> newSharedChain(String name) {
        return this.newSharedChain(name, false);
    }

    public TaskerChain<Object> newSharedChain(String name, boolean priority) {

        // no queue, start task
        if (!this.sharedChainsTasks.containsKey(name)) {
            Object task = this.executor.schedule(() -> this.execSharedChainQueue(name), true);
            this.sharedChainsTasks.put(name, task);
        }

        // create chain with target queue
        return new SharedChain<>(this.executor, this.getSharedChainQueue(name, priority));
    }

    private Queue<Runnable> getSharedChainQueue(String name, boolean priority) {
        Map<String, Queue<Runnable>> queueMap = priority ? this.sharedChainsPriority : this.sharedChains;
        return queueMap.computeIfAbsent(name, (n) -> new ConcurrentLinkedQueue<>());
    }

    private void execSharedChainQueue(String name) {

        // still locked
        AtomicBoolean lock = this.sharedChainsLocks.computeIfAbsent(name, (n) -> new AtomicBoolean(false));
        if (lock.get()) {
            return;
        }

        // lock
        lock.set(true);

        // get queues
        Queue<Runnable> queue = this.getSharedChainQueue(name, false);
        Queue<Runnable> priorityQueue = this.getSharedChainQueue(name, true);

        // run queue
        try {
            // priority first
            while (!priorityQueue.isEmpty()) {
                priorityQueue.poll().run();
            }
            // standard last
            int tasksDone = 0;
            while (!queue.isEmpty()) {
                // take a break to free up priority
                if (tasksDone > 1 && !priorityQueue.isEmpty()) {
                    break;
                }
                // just run
                queue.poll().run();
                tasksDone++;
            }
        }
        // if something bad happens we still want to unlock
        finally {
            lock.set(false);
        }
    }
}
