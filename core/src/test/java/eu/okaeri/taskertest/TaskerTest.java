package eu.okaeri.taskertest;

import eu.okaeri.tasker.core.Tasker;
import eu.okaeri.tasker.core.TaskerExecutor;
import lombok.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public class TaskerTest {

    private Tasker pool;

    @BeforeEach
    public void createPool() {
        this.pool = Tasker.newPool(new TaskerExecutor<Object>() {
            @Override
            public boolean isMain() {
                return true;
            }

            @Override
            public Object schedule(Runnable runnable, boolean async) {
                throw new RuntimeException("Not implemented yet!");
            }

            @Override
            public Object schedule(@NonNull Runnable runnable, @NonNull Duration delay, @NonNull Duration rate, boolean async) {
                throw new RuntimeException("Not implemented yet!");
            }

            @Override
            public Object run(Runnable runnable, Runnable callback, boolean async) {
                runnable.run();
                callback.run();
                return null;
            }

            @Override
            public void cancel(Object task) {
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
            .acceptAsync(data -> {
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
            .acceptSync((Consumer<Object>) (data) -> counter.getAndIncrement())
            .acceptAsync((Consumer<Object>) (data) -> counter.getAndIncrement())
            .acceptSync((Function<Object, Object>) (data) -> counter.getAndIncrement())
            .acceptAsync((Function<Object, Object>) (data) -> counter.getAndIncrement())
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

    @Test
    public void test_unhandled_exception_first() {
        assertThrows(RuntimeException.class, () -> this.pool.newChain()
            .sync(() -> {
                throw new RuntimeException();
            })
            .execute());
    }

    @Test
    public void test_unhandled_exception_second() {
        assertThrows(RuntimeException.class, () -> this.pool.newChain()
            .sync(() -> {
                boolean hmm = true;
            })
            .sync(() -> {
                throw new RuntimeException();
            })
            .execute());
    }

    @Test
    public void test_handled_exception() {
        AtomicReference<Object> watcher = new AtomicReference<>("failed!");
        this.pool.newChain()
            .sync(() -> {
                throw new RuntimeException();
            })
            .handleExceptionSync(exception -> {
                watcher.set(null);
                return null;
            })
            .execute();
        assertNull(watcher.get());
    }

    @Test
    public void test_abort_if_exception() {
        AtomicReference<Object> watcher = new AtomicReference<>();
        this.pool.newChain()
            .sync(() -> {
                throw new RuntimeException();
            })
            .abortIfException()
            .sync(() -> watcher.set("failed!"))
            .execute();
        assertNull(watcher.get());
    }
}
