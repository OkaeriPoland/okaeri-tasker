package eu.okaeri.tasker.paper;

import eu.okaeri.tasker.core.context.TaskerContext;
import eu.okaeri.tasker.core.context.TaskerPlatform;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class FoliaPlatform implements TaskerPlatform {

    protected final TaskerContext defaultContext;

    @Override
    public void cancel(@NonNull Object task) {
        ((ScheduledTask) task).cancel();
    }
}
