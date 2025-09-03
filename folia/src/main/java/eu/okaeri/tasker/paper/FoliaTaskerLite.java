package eu.okaeri.tasker.paper;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class FoliaTaskerLite {

    private static final @Getter ThreadLocal<Plugin> context = new ThreadLocal<>();

    public static Plugin context() {
        Plugin plugin = context.get();
        if (plugin == null) {
            throw new RuntimeException("Unknown context! BukkitTaskerLite#join can only be used in #submit or #eval thread.");
        }
        return plugin;
    }

    public static Runnable withContext(@NonNull Plugin plugin, @NonNull Runnable runnable) {
        return () -> {
            try {
                context.set(plugin);
                runnable.run();
            } finally {
                context.remove();
            }
        };
    }

    public static void submit(@NonNull Plugin plugin, @NonNull Runnable runnable) {
        Bukkit.getServer().getAsyncScheduler().runNow(plugin, t -> withContext(plugin, runnable).run());
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

    @SneakyThrows
    public static void delay(@NonNull Duration time) {
        if (Bukkit.isPrimaryThread()) {
            throw new RuntimeException("Unknown context! FoliaTaskerLite#delay cannot be used in the primary thread.");
        }
        Thread.sleep(time.toMillis());
    }

    // ENTITY
    public static <T> T join(@NonNull Entity entity, @NonNull Supplier<T> supplier) {
        CompletableFuture<T> future = new CompletableFuture<>();
        entity.getScheduler().run(context(), t -> future.complete(supplier.get()), null);
        return future.join();
    }

    public static void sync(@NonNull Entity entity, @NonNull Runnable runnable) {
        join(entity, () -> {
            runnable.run();
            return null;
        });
    }

    public static void detach(@NonNull Entity entity, @NonNull Runnable runnable) {
        entity.getScheduler().run(context(), t -> runnable.run(), null);
    }

    // LOCATION
    public static <T> T join(@NonNull Location location, @NonNull Supplier<T> supplier) {
        CompletableFuture<T> future = new CompletableFuture<>();
        Bukkit.getRegionScheduler().run(context(), location, t -> future.complete(supplier.get()));
        return future.join();
    }

    public static void sync(@NonNull Location location, @NonNull Runnable runnable) {
        join(location, () -> {
            runnable.run();
            return null;
        });
    }

    public static void detach(@NonNull Location location, @NonNull Runnable runnable) {
        Bukkit.getRegionScheduler().run(context(), location, t -> runnable.run());
    }

    // GLOBAL
    public static <T> T join(@NonNull Supplier<T> supplier) {
        CompletableFuture<T> future = new CompletableFuture<>();
        Bukkit.getGlobalRegionScheduler().run(context(), t -> future.complete(supplier.get()));
        return future.join();
    }

    public static void sync(@NonNull Runnable runnable) {
        join(() -> {
            runnable.run();
            return null;
        });
    }

    public static void detach(@NonNull Runnable runnable) {
        Bukkit.getGlobalRegionScheduler().run(context(), t -> runnable.run());
    }
}
