package eu.okaeri.tasker.core;

import eu.okaeri.tasker.core.chain.TaskerChain;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class TaskerFuture<T> implements Future<T> {

    protected final TaskerChain<T> chain;

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return this.chain.cancel();
    }

    @Override
    public boolean isCancelled() {
        return this.chain.isCancelled();
    }

    @Override
    public boolean isDone() {
        return this.chain.isDone();
    }

    @Override
    public T get() {
        return this.chain.await();
    }

    @Override
    public T get(long timeout, TimeUnit unit) {
        return this.chain.await(timeout, unit);
    }
}
