package eu.okaeri.tasker.velocity;

import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.ProxyServer;
import eu.okaeri.tasker.core.Tasker;
import eu.okaeri.tasker.core.TaskerExecutor;
import lombok.NonNull;

public class VelocityTasker extends Tasker {

    protected VelocityTasker(@NonNull TaskerExecutor<?> executor) {
        super(executor);
    }

    public static VelocityTasker newPool(@NonNull ProxyServer server, @NonNull PluginContainer plugin) {
        return new VelocityTasker(new VelocityExecutor(server, plugin));
    }
}
