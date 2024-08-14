package eu.okaeri.tasker.velocity;

import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.ProxyServer;
import eu.okaeri.tasker.core.Tasker;
import eu.okaeri.tasker.core.context.TaskerPlatform;
import lombok.NonNull;

public class VelocityTasker extends Tasker {

    protected VelocityTasker(@NonNull TaskerPlatform platform) {
        super(platform);
    }

    public static VelocityTasker newPool(@NonNull ProxyServer server, @NonNull PluginContainer plugin) {
        return new VelocityTasker(new VelocityPlatform(new VelocityContext(server, plugin)));
    }
}
