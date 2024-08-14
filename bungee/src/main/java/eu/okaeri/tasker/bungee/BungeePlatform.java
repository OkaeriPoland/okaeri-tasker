package eu.okaeri.tasker.bungee;

import eu.okaeri.tasker.core.context.TaskerPlatform;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.scheduler.ScheduledTask;

@Getter
@RequiredArgsConstructor
public class BungeePlatform implements TaskerPlatform {

    protected final BungeeContext defaultContext;

    @Override
    public void cancel(@NonNull Object task) {
        ((ScheduledTask) task).cancel();
    }
}
