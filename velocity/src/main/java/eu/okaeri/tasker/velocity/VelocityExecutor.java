package eu.okaeri.tasker.velocity;

import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import eu.okaeri.tasker.core.TaskerExecutor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

@RequiredArgsConstructor
public class VelocityExecutor implements TaskerExecutor<ScheduledTask> {

    protected final ProxyServer server;
    protected final PluginContainer plugin;

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
            throw new IllegalArgumentException("Velocity does not have a main thread, synchronous execution is not possible!");
        }
        return this.server.getScheduler()
            .buildTask(this.plugin, runnable)
            .delay(Duration.ofMillis(10))
            .repeat(Duration.ofMillis(10))
            .schedule();
    }

    @Override
    public ScheduledTask schedule(@NonNull Runnable runnable, @NonNull Duration delay, @NonNull Duration rate, boolean async) {
        if (!async) {
            throw new IllegalArgumentException("Velocity does not have a main thread, synchronous execution is not possible!");
        }
        return this.server.getScheduler()
            .buildTask(this.plugin, runnable)
            .delay(delay)
            .repeat(rate)
            .schedule();
    }

    @Override
    public ScheduledTask run(@NonNull Runnable runnable, boolean async) {
        if (!async) {
            throw new IllegalArgumentException("Velocity does not have a main thread, synchronous execution is not possible!");
        }
        return this.server.getScheduler()
            .buildTask(this.plugin, runnable)
            .schedule();
    }

    @Override
    public ScheduledTask runLater(@NonNull Runnable runnable, @NonNull Duration delay, boolean async) {
        if (!async) {
            throw new IllegalArgumentException("Velocity does not have a main thread, synchronous execution is not possible!");
        }
        if (delay.isZero()) {
            return this.run(runnable, async);
        }
        return this.server.getScheduler()
            .buildTask(this.plugin, runnable)
            .delay(delay)
            .schedule();
    }

    @Override
    public void cancel(@NonNull ScheduledTask task) {
        task.cancel();
    }
}
