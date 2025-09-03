package eu.okaeri.tasker.paper.context;

import eu.okaeri.tasker.core.context.TaskerContext;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static eu.okaeri.tasker.paper.FoliaTaskerLite.withContext;

@RequiredArgsConstructor
public class AsyncFoliaTaskerContext implements TaskerContext {

    protected final Plugin plugin;

    @Override
    public Object run(@NonNull Runnable runnable) {
        return Bukkit.getAsyncScheduler().runNow(this.plugin, task -> withContext(this.plugin, runnable).run());
    }

    @Override
    public Object runLater(@NonNull Duration delay, @NonNull Runnable runnable) {
        return Bukkit.getAsyncScheduler().runDelayed(this.plugin, task -> withContext(this.plugin, runnable).run(), delay.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public Object schedule(@NonNull Runnable runnable) {
        return Bukkit.getAsyncScheduler().runAtFixedRate(this.plugin, task -> withContext(this.plugin, runnable).run(), 10, 10, TimeUnit.MILLISECONDS);
    }

    @Override
    public Object schedule(@NonNull Runnable runnable, @NonNull Duration delay, @NonNull Duration rate) {
        return Bukkit.getAsyncScheduler().runAtFixedRate(this.plugin, task -> withContext(this.plugin, runnable).run(), delay.toMillis(), rate.toMillis(), TimeUnit.MILLISECONDS);
    }
}
