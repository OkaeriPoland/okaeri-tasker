package eu.okaeri.tasker.bukkit;

import eu.okaeri.tasker.core.TaskerExecutor;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

@RequiredArgsConstructor
public class BukkitExecutor implements TaskerExecutor<BukkitTask> {

    private final Plugin plugin;

    @Override
    public boolean isMain() {
        return Bukkit.isPrimaryThread();
    }

    @Override
    public BukkitTask schedule(Runnable runnable, boolean async) {
        if (async) {
            return Bukkit.getScheduler().runTaskTimerAsynchronously(this.plugin, runnable, 1, 1);
        } else {
            return Bukkit.getScheduler().runTaskTimer(this.plugin, runnable, 1, 1);
        }
    }

    @Override
    public BukkitTask run(Runnable runnable, Runnable callback, boolean async) {
        // prepare callback
        Runnable task = () -> {
            runnable.run();
            callback.run();
        };
        // run
        if (async) {
            return Bukkit.getScheduler().runTaskAsynchronously(this.plugin, task);
        } else {
            return Bukkit.getScheduler().runTask(this.plugin, task);
        }
    }

    @Override
    public void cancel(BukkitTask task) {
        task.cancel();
    }
}
