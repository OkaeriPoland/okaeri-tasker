package eu.okaeri.tasker.bungee;

import eu.okaeri.tasker.core.TaskerExecutor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class BungeeExecutor implements TaskerExecutor<ScheduledTask> {

    protected final Plugin plugin;

    @Override
    public boolean hasMain() {
        return false;
    }

    @Override
    public boolean isMain() {
        return false;
    }

    @Override
    public ScheduledTask schedule(@NonNull Runnable runnable, boolean async) {
        if (!async) {
            throw new IllegalArgumentException("BungeeCord does not have a main thread, synchronous execution is not possible!");
        }
        return ProxyServer.getInstance().getScheduler().schedule(this.plugin, runnable, 10, 10, TimeUnit.MILLISECONDS);
    }

    @Override
    public ScheduledTask schedule(@NonNull Runnable runnable, @NonNull Duration delay, @NonNull Duration rate, boolean async) {
        if (!async) {
            throw new IllegalArgumentException("BungeeCord does not have a main thread, synchronous execution is not possible!");
        }
        return ProxyServer.getInstance().getScheduler().schedule(this.plugin, runnable, delay.toMillis(), rate.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public ScheduledTask run(@NonNull Runnable runnable, boolean async) {
        if (!async) {
            throw new IllegalArgumentException("BungeeCord does not have a main thread, synchronous execution is not possible!");
        }
        return ProxyServer.getInstance().getScheduler().runAsync(this.plugin, runnable);
    }

    @Override
    public ScheduledTask runLater(@NonNull Runnable runnable, @NonNull Duration delay, boolean async) {
        if (!async) {
            throw new IllegalArgumentException("BungeeCord does not have a main thread, synchronous execution is not possible!");
        }
        if (delay.isZero()) {
            return this.run(runnable, async);
        }
        return ProxyServer.getInstance().getScheduler().schedule(this.plugin, runnable, delay.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void cancel(@NonNull ScheduledTask task) {
        task.cancel();
    }
}
