package eu.okaeri.tasker.core;

import eu.okaeri.tasker.core.chain.SharedChain;
import eu.okaeri.tasker.core.chain.TaskerChain;
import eu.okaeri.tasker.core.context.TaskerPlatform;
import eu.okaeri.tasker.core.delayer.Delayer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class Tasker implements Closeable {

    protected final Map<String, Queue<Runnable>> sharedChains = new ConcurrentHashMap<>();
    protected final Map<String, Queue<Runnable>> sharedChainsPriority = new ConcurrentHashMap<>();
    protected final Map<String, Object> sharedChainsTasks = new ConcurrentHashMap<>();
    protected final Map<String, AtomicBoolean> sharedChainsLocks = new ConcurrentHashMap<>();
    protected final @Getter TaskerPlatform platform;

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
        return new TaskerChain<>(this);
    }

    public TaskerChain<Object> newChain(@NonNull Consumer<TaskerChain<Object>> consumer) {
        TaskerChain<Object> chain = new TaskerChain<>(this);
        consumer.accept(chain);
        return chain;
    }

    public TaskerChain<Object> newSharedChain(@NonNull String name) {
        return this.newSharedChain(name, false);
    }

    public TaskerChain<Object> newSharedChain(@NonNull String name, boolean priority) {

        // no shared queue with this name available, start the queue task
        this.sharedChainsTasks.computeIfAbsent(name, s ->
            this.platform.getDefaultContext().schedule(() -> this.execSharedChainQueue(s))
        );

        // create the chain within the target queue
        return new SharedChain<>(this, this.getSharedChainQueue(name, priority));
    }

    public <T> CompletableFuture<T> eval(@NonNull Supplier<T> supplier) {
        CompletableFuture<T> future = new CompletableFuture<>();
        this.submit(() -> future.complete(supplier.get()));
        return future;
    }

    public void submit(@NonNull Runnable runnable) {
        this.newChain().run(runnable).execute();
    }

    public Future<?> submitFuture(@NonNull Runnable runnable) {
        return this.newChain().run(runnable).executeFuture();
    }

    public void submitShared(@NonNull String name, boolean priority, @NonNull Runnable runnable) {
        this.newSharedChain(name, priority)
            .run(runnable)
            .execute();
    }

    public void submitShared(@NonNull String name, @NonNull Runnable runnable) {
        this.submitShared(name, false, runnable);
    }

    protected Queue<Runnable> getSharedChainQueue(@NonNull String name, boolean priority) {
        Map<String, Queue<Runnable>> queueMap = priority ? this.sharedChainsPriority : this.sharedChains;
        return queueMap.computeIfAbsent(name, (n) -> new ConcurrentLinkedQueue<>());
    }

    protected void execSharedChainQueue(@NonNull String name) {

        // still locked
        AtomicBoolean lock = this.sharedChainsLocks.computeIfAbsent(name, (n) -> new AtomicBoolean(false));
        if (!lock.compareAndSet(false, true)) {
            return;
        }

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
        // if something bad happens, we still want to release the lock
        finally {
            lock.set(false);
        }
    }

    @Override
    public void close() throws IOException {
        this.sharedChainsTasks.values().forEach(this.platform::cancel);
    }
}
