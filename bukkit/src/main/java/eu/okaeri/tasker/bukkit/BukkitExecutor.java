package eu.okaeri.tasker.bukkit;

import eu.okaeri.tasker.core.TaskerExecutor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;

@RequiredArgsConstructor
public class BukkitExecutor implements TaskerExecutor<BukkitTask> {

    protected final Plugin plugin;

    @Override
    public boolean isMain() {
        return Bukkit.isPrimaryThread();
    }

    @Override
    public BukkitTask schedule(@NonNull Runnable runnable, boolean async) {
        if (async) {
            return Bukkit.getScheduler().runTaskTimerAsynchronously(this.plugin, runnable, 1, 1);
        } else {
            return Bukkit.getScheduler().runTaskTimer(this.plugin, runnable, 1, 1);
        }
    }

    @Override
    public BukkitTask schedule(@NonNull Runnable runnable, @NonNull Duration delay, @NonNull Duration rate, boolean async) {

        long delayTicks = delay.toMillis() < 50 ? 1 : (delay.toMillis() / 50L);
        long rateTicks = rate.toMillis() < 50 ? 1 : (rate.toMillis() / 50L);

        if (async) {
            return Bukkit.getScheduler().runTaskTimerAsynchronously(this.plugin, runnable, delayTicks, rateTicks);
        } else {
            return Bukkit.getScheduler().runTaskTimer(this.plugin, runnable, delayTicks, rateTicks);
        }
    }

    @Override
    public BukkitTask run(@NonNull Runnable runnable, boolean async) {
        if (async) {
            return Bukkit.getScheduler().runTaskAsynchronously(this.plugin, runnable);
        } else {
            return Bukkit.getScheduler().runTask(this.plugin, runnable);
        }
    }

    @Override
    public BukkitTask runLater(@NonNull Runnable runnable, @NonNull Duration delay, boolean async) {
        if (delay.isZero()) {
            return this.run(runnable, async);
        }
        long delayTicks = delay.toMillis() < 50 ? 1 : (delay.toMillis() / 50L);
        if (async) {
            return Bukkit.getScheduler().runTaskLaterAsynchronously(this.plugin, runnable, delayTicks);
        } else {
            return Bukkit.getScheduler().runTaskLater(this.plugin, runnable, delayTicks);
        }
    }

    @Override
    public void cancel(@NonNull BukkitTask task) {
        task.cancel();
    }
}
