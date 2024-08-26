package eu.okaeri.taskertest;

import eu.okaeri.tasker.core.Tasker;
import eu.okaeri.tasker.core.context.DefaultTaskerPlatform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static eu.okaeri.tasker.core.TaskerDsl.transform;
import static org.junit.jupiter.api.Assertions.*;

public class TaskerTest {

    private Tasker pool;

    @BeforeEach
    public void createPool() {
        this.pool = Tasker.newPool(new DefaultTaskerPlatform());
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
            .await();
        assertEquals("hello     world", result);
    }

    @Test
    public void test_execution_of_all_methods() {
        List<Integer> counter = new ArrayList<>();
        boolean result = this.pool.newChain()
            .run(() -> counter.add(1))
            .accept(data -> counter.add(2))
            .transform(data -> counter.add(3))
            .await();
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
            .execute();
        assertNull(watcher.get());
    }

    @Test
    public void test_unhandled_exception_first() {
        assertThrows(RuntimeException.class, () -> this.pool.newChain()
            .supply(() -> {
                throw new RuntimeException();
            })
            .execute());
    }

    @Test
    public void test_unhandled_exception_second() {
        assertThrows(RuntimeException.class, () -> this.pool.newChain()
            .run(() -> {
                boolean hmm = true;
            })
            .supply(() -> {
                throw new RuntimeException();
            })
            .execute());
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
            .execute();
        assertNull(watcher.get());
    }

    @Test
    public void test_abort_if_exception() {
        AtomicReference<Object> watcher = new AtomicReference<>();
        this.pool.newChain()
            .supply(() -> {
                throw new RuntimeException();
            })
            .abortIfException()
            .run(() -> watcher.set("failed!"))
            .execute();
        assertNull(watcher.get());
    }

    @Test
    public void test_delay() {
        AtomicReference<Instant> start = new AtomicReference<>();
        AtomicReference<Instant> afterDelay = new AtomicReference<>();
        this.pool.newChain()
            .run(() -> start.set(Instant.now()))
            .delay(Duration.ofSeconds(1))
            .run(() -> afterDelay.set(Instant.now()))
            .await();
        Duration duration = Duration.between(start.get(), afterDelay.get());
        assertTrue(duration.compareTo(Duration.ofMillis(900)) > 0, "duration is more than 900ms");
        assertTrue(duration.compareTo(Duration.ofMillis(1100)) < 0, "duration is less than 1100ms");
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
