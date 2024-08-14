package eu.okaeri.tasker.bukkit;

import eu.okaeri.tasker.core.context.TaskerContext;
import eu.okaeri.tasker.core.context.TaskerPlatform;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.scheduler.BukkitTask;

@Getter
@RequiredArgsConstructor
public class BukkitPlatform implements TaskerPlatform {

    protected final TaskerContext defaultContext;

    @Override
    public void cancel(@NonNull Object task) {
        ((BukkitTask) task).cancel();
    }
}
