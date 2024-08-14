package eu.okaeri.tasker.bukkit.context;

import eu.okaeri.tasker.core.context.TaskerContext;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.time.Duration;

@RequiredArgsConstructor
public class SyncBukkitTaskerContext implements TaskerContext {

    protected final Plugin plugin;

    @Override
    public Object run(@NonNull Runnable runnable) {
        return this.plugin.getServer().getScheduler().runTask(this.plugin, runnable);
    }

    @Override
    public Object runLater(@NonNull Duration delay, @NonNull Runnable runnable) {
        long delayTicks = delay.toMillis() < 50 ? 1 : (delay.toMillis() / 50L);
        return this.plugin.getServer().getScheduler().runTaskLater(this.plugin, runnable, delayTicks);
    }

    @Override
    public Object schedule(@NonNull Runnable runnable) {
        return Bukkit.getScheduler().runTaskTimer(this.plugin, runnable, 1, 1);
    }

    @Override
    public Object schedule(@NonNull Runnable runnable, @NonNull Duration delay, @NonNull Duration rate) {
        long delayTicks = delay.toMillis() < 50 ? 1 : (delay.toMillis() / 50L);
        long rateTicks = rate.toMillis() < 50 ? 1 : (rate.toMillis() / 50L);
        return Bukkit.getScheduler().runTaskTimer(this.plugin, runnable, delayTicks, rateTicks);
    }
}
