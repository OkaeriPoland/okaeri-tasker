package eu.okaeri.tasker.paper;

import eu.okaeri.tasker.core.Tasker;
import eu.okaeri.tasker.core.Taskerable;
import eu.okaeri.tasker.core.context.TaskerContext;
import eu.okaeri.tasker.core.context.TaskerPlatform;
import eu.okaeri.tasker.paper.context.AsyncFoliaTaskerContext;
import eu.okaeri.tasker.paper.context.GlobalFoliaTaskerContext;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public class FoliaTasker extends Tasker {

    protected final Plugin plugin;
    protected final TaskerContext global;
    protected final TaskerContext async;

    protected FoliaTasker(@NonNull Plugin plugin, @NonNull TaskerPlatform platform) {
        super(platform);
        this.plugin = plugin;
        this.global = new GlobalFoliaTaskerContext(plugin);
        this.async = new AsyncFoliaTaskerContext(plugin);
    }

    public static FoliaTasker newPool(@NonNull Plugin plugin) {
        return new FoliaTasker(plugin, new FoliaPlatform(new AsyncFoliaTaskerContext(plugin)));
    }

    @SuppressWarnings("unchecked")
    public <X, T extends Taskerable<X>> T async(@NonNull T taskerable) {
        return (T) taskerable.context(() -> this.async);
    }

    @SuppressWarnings("unchecked")
    public <X, T extends Taskerable<X>> T global(@NonNull T taskerable) {
        return (T) taskerable.context(() -> this.global);
    }

    public <X, T extends Taskerable<X>> T entity(@NonNull Entity entity, @NonNull T taskerable) {
        return FoliaTaskerDsl.entity(this.plugin, entity, taskerable);
    }

    public <X, T extends Taskerable<X>> T location(@NonNull Location location, @NonNull T taskerable) {
        return FoliaTaskerDsl.location(this.plugin, location, taskerable);
    }
}
