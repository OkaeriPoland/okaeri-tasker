package eu.okaeri.tasker.bukkit;

import eu.okaeri.tasker.core.Tasker;
import eu.okaeri.tasker.core.TaskerExecutor;
import org.bukkit.plugin.Plugin;

public class BukkitTasker extends Tasker {

    protected BukkitTasker(TaskerExecutor<?> executor, Plugin plugin) {
        super(executor);
    }

    public static BukkitTasker newPool(Plugin plugin) {
        return new BukkitTasker(new BukkitExecutor(plugin), plugin);
    }
}
