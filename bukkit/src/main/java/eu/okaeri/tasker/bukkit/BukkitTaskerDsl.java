package eu.okaeri.tasker.bukkit;

import eu.okaeri.tasker.bukkit.context.AsyncTaskerContext;
import eu.okaeri.tasker.bukkit.context.SyncTaskerContext;
import eu.okaeri.tasker.core.Taskerable;
import lombok.NonNull;
import org.bukkit.plugin.Plugin;

public final class BukkitTaskerDsl {

    @SuppressWarnings("unchecked")
    public static <X, T extends Taskerable<X>> T sync(@NonNull Plugin plugin, @NonNull T taskerable) {
        return (T) taskerable.context(() -> new SyncTaskerContext(plugin));
    }

    @SuppressWarnings("unchecked")
    public static <X, T extends Taskerable<X>> T async(@NonNull Plugin plugin, @NonNull T taskerable) {
        return (T) taskerable.context(() -> new AsyncTaskerContext(plugin));
    }
}
