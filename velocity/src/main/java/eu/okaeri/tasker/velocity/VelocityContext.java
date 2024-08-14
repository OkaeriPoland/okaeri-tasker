package eu.okaeri.tasker.velocity;

import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.ProxyServer;
import eu.okaeri.tasker.core.context.TaskerContext;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

@RequiredArgsConstructor
public class VelocityContext implements TaskerContext {

    protected final ProxyServer server;
    protected final PluginContainer plugin;

    @Override
    public Object run(@NonNull Runnable runnable) {
        return this.server.getScheduler()
            .buildTask(this.plugin, runnable)
            .schedule();
    }

    @Override
    public Object runLater(@NonNull Duration delay, @NonNull Runnable runnable) {
        return this.server.getScheduler()
            .buildTask(this.plugin, runnable)
            .delay(delay)
            .schedule();
    }

    @Override
    public Object schedule(@NonNull Runnable runnable) {
        return this.server.getScheduler()
            .buildTask(this.plugin, runnable)
            .delay(Duration.ofMillis(10))
            .repeat(Duration.ofMillis(10))
            .schedule();
    }

    @Override
    public Object schedule(@NonNull Runnable runnable, @NonNull Duration delay, @NonNull Duration rate) {
        return this.server.getScheduler()
            .buildTask(this.plugin, runnable)
            .delay(delay)
            .repeat(rate)
            .schedule();
    }
}
