package eu.okaeri.tasker.bukkit;

import eu.okaeri.tasker.core.Tasker;
import eu.okaeri.tasker.core.TaskerExecutor;
import eu.okaeri.tasker.core.delayer.Delayer;
import lombok.NonNull;
import org.bukkit.plugin.Plugin;

import java.time.Duration;

public class BukkitTasker extends Tasker {

    protected BukkitTasker(@NonNull TaskerExecutor<?> executor, @NonNull Plugin plugin) {
        super(executor);
    }

    public static BukkitTasker newPool(@NonNull Plugin plugin) {
        return new BukkitTasker(new BukkitExecutor(plugin), plugin);
    }

    public Delayer newDelayer(@NonNull Duration duration, long checkRateTicks) {
        return this.newDelayer(duration, Duration.ofMillis(50L * checkRateTicks));
    }
}
