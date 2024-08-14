package eu.okaeri.tasker.bukkit;

import eu.okaeri.tasker.bukkit.context.AsyncBukkitTaskerContext;
import eu.okaeri.tasker.bukkit.context.SyncBukkitTaskerContext;
import eu.okaeri.tasker.core.Tasker;
import eu.okaeri.tasker.core.Taskerable;
import eu.okaeri.tasker.core.context.TaskerContext;
import eu.okaeri.tasker.core.context.TaskerPlatform;
import eu.okaeri.tasker.core.delayer.Delayer;
import lombok.NonNull;
import org.bukkit.plugin.Plugin;

import java.time.Duration;

public class BukkitTasker extends Tasker {

    protected final Plugin plugin;
    protected final TaskerContext sync;
    protected final TaskerContext async;

    protected BukkitTasker(@NonNull Plugin plugin, @NonNull TaskerPlatform platform) {
        super(platform);
        this.plugin = plugin;
        this.sync = new SyncBukkitTaskerContext(plugin);
        this.async = new AsyncBukkitTaskerContext(plugin);
    }

    public static BukkitTasker newPool(@NonNull Plugin plugin) {
        return new BukkitTasker(plugin, new BukkitPlatform(new AsyncBukkitTaskerContext(plugin)));
    }

    public Delayer newDelayer(@NonNull Duration duration, long checkRateTicks) {
        return this.newDelayer(duration, Duration.ofMillis(50L * checkRateTicks));
    }

    @SuppressWarnings("unchecked")
    public <X, T extends Taskerable<X>> T sync(@NonNull T taskerable) {
        return (T) taskerable.context(() -> this.sync);
    }

    @SuppressWarnings("unchecked")
    public <X, T extends Taskerable<X>> T async(@NonNull T taskerable) {
        return (T) taskerable.context(() -> this.async);
    }
}
