package eu.okaeri.tasker.bungee;

import eu.okaeri.tasker.core.context.TaskerContext;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class BungeeContext implements TaskerContext {

    protected final Plugin plugin;

    @Override
    public Object run(@NonNull Runnable runnable) {
        return ProxyServer.getInstance().getScheduler().runAsync(this.plugin, runnable);
    }

    @Override
    public Object runLater(@NonNull Duration delay, @NonNull Runnable runnable) {
        return ProxyServer.getInstance().getScheduler().schedule(this.plugin, runnable, delay.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public Object schedule(@NonNull Runnable runnable) {
        return ProxyServer.getInstance().getScheduler().schedule(this.plugin, runnable, 10, 10, TimeUnit.MILLISECONDS);
    }

    @Override
    public Object schedule(@NonNull Runnable runnable, @NonNull Duration delay, @NonNull Duration rate) {
        return ProxyServer.getInstance().getScheduler().schedule(this.plugin, runnable, delay.toMillis(), rate.toMillis(), TimeUnit.MILLISECONDS);
    }
}
