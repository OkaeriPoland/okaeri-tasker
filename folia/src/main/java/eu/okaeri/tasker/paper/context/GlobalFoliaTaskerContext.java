package eu.okaeri.tasker.paper.context;

import eu.okaeri.tasker.core.context.TaskerContext;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.time.Duration;

@RequiredArgsConstructor
public class GlobalFoliaTaskerContext implements TaskerContext {

    protected final Plugin plugin;

    @Override
    public Object run(@NonNull Runnable runnable) {
        return Bukkit.getGlobalRegionScheduler().run(this.plugin, task -> runnable.run());
    }

    @Override
    public Object runLater(@NonNull Duration delay, @NonNull Runnable runnable) {
        return Bukkit.getGlobalRegionScheduler().runDelayed(this.plugin, task -> runnable.run(), delay.toMillis());
    }

    @Override
    public Object schedule(@NonNull Runnable runnable) {
        return Bukkit.getGlobalRegionScheduler().runAtFixedRate(this.plugin, task -> runnable.run(), 10, 10);
    }

    @Override
    public Object schedule(@NonNull Runnable runnable, @NonNull Duration delay, @NonNull Duration rate) {
        return Bukkit.getGlobalRegionScheduler().runAtFixedRate(this.plugin, task -> runnable.run(), delay.toMillis(), rate.toMillis());
    }
}
