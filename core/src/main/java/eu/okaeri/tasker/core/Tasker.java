package eu.okaeri.tasker.core;

import eu.okaeri.tasker.core.chain.SharedChain;
import eu.okaeri.tasker.core.chain.TaskerChain;
import eu.okaeri.tasker.core.context.TaskerPlatform;
import eu.okaeri.tasker.core.delayer.Delayer;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class Tasker {

    protected final Map<String, Queue<Runnable>> sharedChains = new ConcurrentHashMap<>();
    protected final Map<String, Queue<Runnable>> sharedChainsPriority = new ConcurrentHashMap<>();
    protected final Map<String, Object> sharedChainsTasks = new ConcurrentHashMap<>();
    protected final Map<String, AtomicBoolean> sharedChainsLocks = new ConcurrentHashMap<>();
    protected final TaskerPlatform platform;

    public static Tasker newPool(@NonNull TaskerPlatform platform) {
        return new Tasker(platform);
    }

    public Delayer newDelayer(@NonNull Duration duration) {
        return Delayer.of(this.platform, duration);
    }

    public Delayer newDelayer(@NonNull Duration duration, @NonNull Duration checkRate) {
        return Delayer.of(this.platform, duration, checkRate);
    }

    public TaskerChain<Object> newChain() {
        return new TaskerChain<>(this.platform);
    }

    public TaskerChain<Object> newChain(@NonNull Consumer<TaskerChain<Object>> consumer) {
        TaskerChain<Object> chain = new TaskerChain<>(this.platform);
        consumer.accept(chain);
        return chain;
    }

    public TaskerChain<Object> newSharedChain(@NonNull String name) {
        return this.newSharedChain(name, false);
    }

    public TaskerChain<Object> newSharedChain(@NonNull String name, boolean priority) {

        // no queue, start task
        if (!this.sharedChainsTasks.containsKey(name)) {
            Object task = this.platform.getDefaultContext().schedule(() -> this.execSharedChainQueue(name));
            this.sharedChainsTasks.put(name, task);
        }

        // create chain with target queue
        return new SharedChain<>(this.platform, this.getSharedChainQueue(name, priority));
    }

    protected Queue<Runnable> getSharedChainQueue(@NonNull String name, boolean priority) {
        Map<String, Queue<Runnable>> queueMap = priority ? this.sharedChainsPriority : this.sharedChains;
        return queueMap.computeIfAbsent(name, (n) -> new ConcurrentLinkedQueue<>());
    }

    protected void execSharedChainQueue(@NonNull String name) {

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
