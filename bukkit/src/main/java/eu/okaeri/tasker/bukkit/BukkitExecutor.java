package eu.okaeri.tasker.bukkit;

import eu.okaeri.tasker.core.TaskerExecutor;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

@RequiredArgsConstructor
public class BukkitExecutor implements TaskerExecutor {

    private final Plugin plugin;

    @Override
    public boolean isMain() {
        return Bukkit.isPrimaryThread();
    }

    @Override
    public void schedule(Runnable runnable, boolean async) {
        if (async) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(this.plugin, runnable, 1, 1);
        } else {
            Bukkit.getScheduler().runTaskTimer(this.plugin, runnable, 1, 1);
        }
    }

    @Override
    public void run(Runnable runnable, Runnable callback, boolean async) {
        // prepare callback
        Runnable task = () -> {
            runnable.run();
            callback.run();
        };
        // run
        if (async) {
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, task);
        } else {
            Bukkit.getScheduler().runTask(this.plugin, task);
        }
    }
}
