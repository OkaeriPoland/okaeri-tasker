package eu.okaeri.taskertest;

import eu.okaeri.tasker.core.Tasker;
import eu.okaeri.tasker.core.context.DefaultTaskerPlatform;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static eu.okaeri.tasker.core.TaskerDsl.accept;
import static eu.okaeri.tasker.core.TaskerDsl.transform;
import static org.junit.jupiter.api.Assertions.*;

public class TaskerTest {

    private Tasker pool;

    @BeforeEach
    public void createPool() {
        this.pool = Tasker.newPool(new DefaultTaskerPlatform());
    }

    @AfterEach
    @SneakyThrows
    public void closePool() {
        this.pool.close();
    }

    @Test
    public void test_basic() {
        String result = this.pool.newChain()
            .supply(() -> {
                StringBuilder builder = new StringBuilder();
                builder.append("hello");
                for (int i = 0; i < 5; i++) {
                    builder.append(" ");
                }
                builder.append("world");
                return builder.toString();
            })
            .accept(data -> {
                System.out.println(data.length());
                System.out.println(data);
            })
            .run(() -> {
                boolean hmm = true;
            })
            .transform(data -> {
                System.out.println(Thread.currentThread().getName());
                return data;
            })
            .join();
        assertEquals("hello     world", result);
    }

    @Test
    public void test_execution_of_all_methods() {
        List<Integer> counter = new ArrayList<>();
        boolean result = this.pool.newChain()
            .run(() -> counter.add(1))
            .accept(data -> counter.add(2))
            .transform(data -> counter.add(3))
            .join();
        assertEquals(Arrays.asList(1, 2, 3), counter);
        assertTrue(result);
    }

    @Test
    public void test_abort_on_null() {
        AtomicReference<Object> watcher = new AtomicReference<>();
        this.pool.newChain()
            .supply(() -> null)
            .abortIfNull()
            .run(() -> watcher.set("failed!"))
            .join();
        assertNull(watcher.get());
    }

    @Test
    public void test_unhandled_exception_first() {
        assertThrows(CompletionException.class, () -> this.pool.newChain()
            .supply(() -> {
                throw new RuntimeException();
            })
            .join());
    }

    @Test
    public void test_unhandled_exception_second() {
        assertThrows(ExecutionException.class, () -> this.pool.newChain()
            .run(() -> {
                boolean hmm = true;
            })
            .supply(() -> {
                throw new RuntimeException();
            })
            .await());
    }

    @Test
    public void test_handled_exception() {
        AtomicReference<Object> watcher = new AtomicReference<>("failed!");
        this.pool.newChain()
            .supply(() -> {
                throw new RuntimeException();
            })
            .handleException(transform(exception -> {
                watcher.set(null);
                return null;
            }))
            .join();
        assertNull(watcher.get());
    }

    @Test
    public void test_abort_if_exception() {
        AtomicReference<Object> watcher = new AtomicReference<>();
        this.pool.newChain()
            .supply(() -> {
                throw new RuntimeException("this should be handled");
            })
            .abortIfException()
            .run(() -> watcher.set("failed!"))
            .join();
        assertNull(watcher.get());
    }

    @Test
    public void test_abort_if_exception_then() {
        AtomicReference<Object> watcher = new AtomicReference<>();
        AtomicReference<Throwable> exception = new AtomicReference<>();
        this.pool.newChain()
            .supply(() -> {
                throw new RuntimeException("this should be handled");
            })
            .abortIfExceptionThen(accept(exception::set))
            .run(() -> watcher.set("failed!"))
            .join();
        assertNull(watcher.get());
        assertNotNull(exception.get());
        assertEquals(RuntimeException.class, exception.get().getClass());
    }

    @Test
    public void test_abort_if_exception_then_data() {
        AtomicReference<Object> watcher = new AtomicReference<>();
        AtomicReference<Throwable> exception = new AtomicReference<>();
        AtomicReference<Integer> data = new AtomicReference<>();
        this.pool.newChain()
            .supply(() -> 3)
            .<Integer>supply(() -> {
                throw new RuntimeException("this should be handled");
            })
            .abortIfExceptionThen(accept((ex, i) -> {
                exception.set(ex);
                data.set(i);
            }))
            .transform(i -> i + 1)
            .run(() -> watcher.set("failed!"))
            .join();
        assertNull(watcher.get());
        assertNotNull(exception.get());
        assertEquals(RuntimeException.class, exception.get().getClass());
        assertEquals(3, data.get());
    }

    @Test
    public void test_delay() {
        AtomicReference<Instant> start = new AtomicReference<>();
        AtomicReference<Instant> afterDelay = new AtomicReference<>();
        this.pool.newChain()
            .run(() -> start.set(Instant.now()))
            .delay(Duration.ofSeconds(1))
            .run(() -> afterDelay.set(Instant.now()))
            .join();
        Duration duration = Duration.between(start.get(), afterDelay.get());
        assertTrue(duration.compareTo(Duration.ofMillis(900)) > 0, "duration is more than 900ms");
        assertTrue(duration.compareTo(Duration.ofMillis(1100)) < 0, "duration is less than 1100ms");
    }

    @Test
    public void test_future() {
        Integer value = this.pool.newChain()
            .supply(() -> 1)
            .join();
        assertEquals(1, value);
    }

    @Test
    public void test_shared() {
        AtomicInteger counter = new AtomicInteger();
        this.pool.newSharedChain("test")
            .run(counter::incrementAndGet)
            .join();
        this.pool.newSharedChain("test")
            .run(counter::incrementAndGet)
            .join();
        assertEquals(2, counter.get());
    }

    @Test
    public void test_shared_actually_queues() {
        AtomicInteger counter = new AtomicInteger();
        CompletableFuture<Object> future1 = this.pool.newSharedChain("test")
            .delay(Duration.ofMillis(100))
            .run(() -> counter.set(1))
            .executeFuture();
        CompletableFuture<Object> future2 = this.pool.newSharedChain("test")
            .run(() -> counter.set(2))
            .executeFuture();
        CompletableFuture.allOf(future1, future2).join();
        assertEquals(2, counter.get());
    }

//    @Test
//    @SuppressWarnings("deprecation")
//    public void test_unsafe_insert() {
//        String result = this.pool.newChain()
//            .$(raw(accessor -> accessor.insert(() -> {
//                AtomicInteger counter = new AtomicInteger(0);
//                AtomicReference<Runnable> task = new AtomicReference<>();
//                task.set(() -> {
//                    accessor.insert(run(() -> {
//                        String newValue = accessor.dataOr("") + "!";
//                        accessor.data(newValue);
//                    }));
//                    if (counter.incrementAndGet() < 3) {
//                        task.get().run();
//                    }
//                });
//                task.get().run();
//            })))
//            .transformSync(text -> text.replace("!", "?"))
//            .await();
//        assertEquals("???", result);
//    }
}
