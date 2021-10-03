package eu.okaeri.tasker.bukkit;

import eu.okaeri.core.TaskerExecutor;
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
