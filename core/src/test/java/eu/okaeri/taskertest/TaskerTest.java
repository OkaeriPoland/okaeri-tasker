package eu.okaeri.taskertest;

import eu.okaeri.core.Tasker;
import eu.okaeri.core.TaskerExecutor;
import eu.okaeri.core.TaskerPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TaskerTest {

    private TaskerPool pool;

    @BeforeEach
    public void createPool() {
        this.pool = Tasker.newPool(new TaskerExecutor() {
            @Override
            public boolean isMain() {
                return true;
            }

            @Override
            public void run(Runnable runnable, Runnable callback, boolean async) {
                runnable.run();
                callback.run();
            }
        });
    }

    @Test
    public void test_basic() {
        String result = this.pool.newChain()
                .sync(() -> {
                    StringBuilder builder = new StringBuilder();
                    builder.append("hello");
                    for (int i = 0; i < 5; i++) {
                        builder.append(" ");
                    }
                    builder.append("world");
                    return builder.toString();
                })
                .acceptAsync((String data) -> {
                    System.out.println(Thread.currentThread().getName());
                    System.out.println(data.length());
                    System.out.println(data);
                    return data;
                })
                .await();
        assertEquals("hello     world", result);
    }

    @Test
    public void test_execution_of_all_methods() {
        AtomicInteger counter = new AtomicInteger(0);
        Object result = this.pool.newChain()
                .sync((Runnable) counter::getAndIncrement)
                .async((Runnable) counter::getAndIncrement)
                .acceptSync((Consumer<?>) (data) -> counter.getAndIncrement())
                .acceptAsync((Consumer<?>) (data) -> counter.getAndIncrement())
                .acceptSync((Function<?, ?>) (data) -> counter.getAndIncrement())
                .acceptAsync((Function<?, ?>) (data) -> counter.getAndIncrement())
                .await();
        assertEquals(6, counter.get());
        assertEquals(5, result);
    }

    @Test
    public void test_abort_on_null_sync_async() {
        AtomicReference<Object> watcher = new AtomicReference<>();
        Object result = this.pool.newChain()
                .sync(() -> null)
                .abortIfNull()
                .acceptAsync((Object data) -> {
                    watcher.set("failed!");
                    return data;
                })
                .await();
        assertNull(result);
        assertNull(watcher.get());
    }

    @Test
    public void test_abort_on_null_sync_sync() {
        AtomicReference<Object> watcher = new AtomicReference<>();
        Object result = this.pool.newChain()
                .sync(() -> null)
                .abortIfNull()
                .acceptSync((Object data) -> {
                    watcher.set("failed!");
                    return data;
                })
                .await();
        assertNull(result);
        assertNull(watcher.get());
    }

    @Test
    public void test_abort_on_null_async_sync() {
        AtomicReference<Object> watcher = new AtomicReference<>();
        Object result = this.pool.newChain()
                .async(() -> null)
                .abortIfNull()
                .acceptSync((Object data) -> {
                    watcher.set("failed!");
                    return data;
                })
                .await();
        assertNull(result);
        assertNull(watcher.get());
    }

    @Test
    public void test_abort_on_null_async_async() {
        AtomicReference<Object> watcher = new AtomicReference<>();
        Object result = this.pool.newChain()
                .async(() -> null)
                .abortIfNull()
                .acceptAsync((Object data) -> {
                    watcher.set("failed!");
                    return data;
                })
                .await();
        assertNull(result);
        assertNull(watcher.get());
    }
}
