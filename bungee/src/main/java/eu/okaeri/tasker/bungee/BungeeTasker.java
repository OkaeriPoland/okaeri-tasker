package eu.okaeri.tasker.bungee;

import eu.okaeri.tasker.core.Tasker;
import eu.okaeri.tasker.core.TaskerExecutor;
import lombok.NonNull;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeTasker extends Tasker {

    protected BungeeTasker(@NonNull TaskerExecutor<?> executor) {
        super(executor);
    }

    public static BungeeTasker newPool(@NonNull Plugin plugin) {
        return new BungeeTasker(new BungeeExecutor(plugin));
    }
}
