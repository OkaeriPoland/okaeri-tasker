package eu.okaeri.tasker.paper;

import eu.okaeri.tasker.core.Taskerable;
import eu.okaeri.tasker.paper.context.AsyncFoliaTaskerContext;
import eu.okaeri.tasker.paper.context.EntityFoliaTaskerContext;
import eu.okaeri.tasker.paper.context.GlobalFoliaTaskerContext;
import eu.okaeri.tasker.paper.context.LocationFoliaTaskerContext;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public final class FoliaTaskerDsl {

    @SuppressWarnings("unchecked")
    public static <X, T extends Taskerable<X>> T async(@NonNull Plugin plugin, @NonNull Entity entity, @NonNull T taskerable) {
        return (T) taskerable.context(() -> new AsyncFoliaTaskerContext(plugin));
    }

    @SuppressWarnings("unchecked")
    public static <X, T extends Taskerable<X>> T global(@NonNull Plugin plugin, @NonNull Entity entity, @NonNull T taskerable) {
        return (T) taskerable.context(() -> new GlobalFoliaTaskerContext(plugin));
    }

    @SuppressWarnings("unchecked")
    public static <X, T extends Taskerable<X>> T entity(@NonNull Plugin plugin, @NonNull Entity entity, @NonNull T taskerable) {
        return (T) taskerable.context(() -> new EntityFoliaTaskerContext(plugin, entity.getScheduler()));
    }

    @SuppressWarnings("unchecked")
    public static <X, T extends Taskerable<X>> T location(@NonNull Plugin plugin, @NonNull Location location, @NonNull T taskerable) {
        return (T) taskerable.context(() -> new LocationFoliaTaskerContext(plugin, location));
    }
}
