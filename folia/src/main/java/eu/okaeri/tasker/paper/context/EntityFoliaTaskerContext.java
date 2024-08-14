package eu.okaeri.tasker.paper.context;

import eu.okaeri.tasker.core.context.TaskerContext;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;

import java.time.Duration;

// TODO: handle retired instead of aborting?
@RequiredArgsConstructor
public class EntityFoliaTaskerContext implements TaskerContext {

    protected final Plugin plugin;
    protected final EntityScheduler entity;

    @Override
    public Object run(@NonNull Runnable runnable) {
        return this.entity.run(this.plugin, task -> runnable.run(), () -> {});
    }

    @Override
    public Object runLater(@NonNull Duration delay, @NonNull Runnable runnable) {
        long delayTicks = delay.toMillis() < 50 ? 1 : (delay.toMillis() / 50L);
        return this.entity.runDelayed(this.plugin, task -> runnable.run(), () -> {}, delayTicks);
    }

    @Override
    public Object schedule(@NonNull Runnable runnable) {
        return this.entity.runAtFixedRate(this.plugin, task -> runnable.run(), () -> {}, 1, 1);
    }

    @Override
    public Object schedule(@NonNull Runnable runnable, @NonNull Duration delay, @NonNull Duration rate) {
        long delayTicks = delay.toMillis() < 50 ? 1 : (delay.toMillis() / 50L);
        long rateTicks = rate.toMillis() < 50 ? 1 : (rate.toMillis() / 50L);
        return this.entity.runAtFixedRate(this.plugin, task -> runnable.run(), () -> {}, delayTicks, rateTicks);
    }
}
