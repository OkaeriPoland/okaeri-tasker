package eu.okaeri.tasker.bukkit;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class BukkitTaskerLite {

    private static final @Getter ThreadLocal<Plugin> context = new ThreadLocal<>();

    public static void submit(@NonNull Plugin plugin, @NonNull Runnable runnable) {
        Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                context.set(plugin);
                runnable.run();
            } finally {
                context.remove();
            }
        });
    }

    public static CompletableFuture<?> submitFuture(@NonNull Plugin plugin, @NonNull Runnable runnable) {
        return eval(plugin, () -> {
            runnable.run();
            return null;
        });
    }

    public static <T> CompletableFuture<T> eval(@NonNull Plugin plugin, @NonNull Supplier<T> supplier) {
        CompletableFuture<T> future = new CompletableFuture<>();
        submit(plugin, () -> future.complete(supplier.get()));
        return future;
    }

    public static <T> CompletableFuture<T> evalSync(@NonNull Plugin plugin, @NonNull Supplier<T> supplier) {
        CompletableFuture<T> future = new CompletableFuture<>();
        Bukkit.getServer().getScheduler().runTask(plugin, () -> future.complete(supplier.get()));
        return future;
    }

    public static <T> T sync(@NonNull Supplier<T> supplier) {

        Plugin plugin = context.get();
        if (plugin == null) {
            throw new RuntimeException("Unknown context! BukkitTaskerLite#await can only be used in #submit or #eval thread.");
        }

        CompletableFuture<T> future = new CompletableFuture<>();
        Bukkit.getServer().getScheduler().runTask(plugin, () -> future.complete(supplier.get()));

        return future.join();
    }

    public static void sync(@NonNull Runnable runnable) {
        sync(() -> {
            runnable.run();
            return null;
        });
    }

    @SneakyThrows
    public static void delay(@NonNull Duration time) {
        if (Bukkit.isPrimaryThread()) {
            throw new RuntimeException("Unknown context! BukkitTaskerLite#delay cannot be used in the primary thread.");
        }
        Thread.sleep(time.toMillis());
    }
}
