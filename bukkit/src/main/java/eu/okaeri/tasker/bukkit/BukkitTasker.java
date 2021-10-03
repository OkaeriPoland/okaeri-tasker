package eu.okaeri.tasker.bukkit;

import eu.okaeri.core.Tasker;
import eu.okaeri.core.TaskerExecutor;
import org.bukkit.plugin.Plugin;

public class BukkitTasker extends Tasker {

    private final Plugin plugin;

    protected BukkitTasker(TaskerExecutor executor, Plugin plugin) {
        super(executor);
        this.plugin = plugin;
    }

    public static BukkitTasker newPool(Plugin plugin) {
        return new BukkitTasker(new BukkitExecutor(plugin), plugin);
    }
}
