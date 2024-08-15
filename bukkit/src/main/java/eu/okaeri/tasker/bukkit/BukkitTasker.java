package eu.okaeri.tasker.bukkit;

import eu.okaeri.tasker.bukkit.chain.BukkitSharedChain;
import eu.okaeri.tasker.bukkit.chain.BukkitTaskerChain;
import eu.okaeri.tasker.bukkit.context.AsyncBukkitTaskerContext;
import eu.okaeri.tasker.bukkit.context.SyncBukkitTaskerContext;
import eu.okaeri.tasker.core.Tasker;
import eu.okaeri.tasker.core.Taskerable;
import eu.okaeri.tasker.core.context.TaskerContext;
import eu.okaeri.tasker.core.context.TaskerPlatform;
import eu.okaeri.tasker.core.delayer.Delayer;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.plugin.Plugin;

import java.time.Duration;

@Getter
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

    @Override
    public BukkitTaskerChain<Object> newChain() {
        return new BukkitTaskerChain<>(this);
    }

    public BukkitTaskerChain<Object> newSharedChain(@NonNull String name) {
        return this.newSharedChain(name, false);
    }

    public BukkitTaskerChain<Object> newSharedChain(@NonNull String name, boolean priority) {

        // no queue, start task
        if (!this.sharedChainsTasks.containsKey(name)) {
            Object task = this.platform.getDefaultContext().schedule(() -> this.execSharedChainQueue(name));
            this.sharedChainsTasks.put(name, task);
        }

        // create chain with target queue
        return new BukkitSharedChain<>(this, this.getSharedChainQueue(name, priority));
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
