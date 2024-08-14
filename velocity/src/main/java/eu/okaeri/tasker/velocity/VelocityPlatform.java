package eu.okaeri.tasker.velocity;

import com.velocitypowered.api.scheduler.ScheduledTask;
import eu.okaeri.tasker.core.context.TaskerContext;
import eu.okaeri.tasker.core.context.TaskerPlatform;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class VelocityPlatform implements TaskerPlatform {

    protected final TaskerContext defaultContext;

    @Override
    public void cancel(@NonNull Object task) {
        ((ScheduledTask) task).cancel();
    }
}
