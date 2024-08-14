package eu.okaeri.tasker.paper.context;

import eu.okaeri.tasker.core.context.TaskerContext;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.time.Duration;

@RequiredArgsConstructor
public class LocationFoliaTaskerContext implements TaskerContext {

    protected final Plugin plugin;
    protected final Location location;

    @Override
    public Object run(@NonNull Runnable runnable) {
        return Bukkit.getRegionScheduler().run(this.plugin, this.location, task -> runnable.run());
    }

    @Override
    public Object runLater(@NonNull Duration delay, @NonNull Runnable runnable) {
        long delayTicks = delay.toMillis() < 50 ? 1 : (delay.toMillis() / 50L);
        return Bukkit.getRegionScheduler().runDelayed(this.plugin, this.location, task -> runnable.run(), delayTicks);
    }

    @Override
    public Object schedule(@NonNull Runnable runnable) {
        return Bukkit.getRegionScheduler().runAtFixedRate(this.plugin, this.location, task -> runnable.run(), 1, 1);
    }

    @Override
    public Object schedule(@NonNull Runnable runnable, @NonNull Duration delay, @NonNull Duration rate) {
        long delayTicks = delay.toMillis() < 50 ? 1 : (delay.toMillis() / 50L);
        long rateTicks = rate.toMillis() < 50 ? 1 : (rate.toMillis() / 50L);
        return Bukkit.getRegionScheduler().runAtFixedRate(this.plugin, this.location, task -> runnable.run(), delayTicks, rateTicks);
    }
}
