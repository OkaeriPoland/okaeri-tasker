package eu.okaeri.tasker.bukkit;

import eu.okaeri.core.TaskerPool;
import org.bukkit.plugin.Plugin;

public final class BukkitTasker {

    public static TaskerPool newPool(Plugin plugin) {
        return new TaskerPool(new BukkitExecutor(plugin));
    }
}
