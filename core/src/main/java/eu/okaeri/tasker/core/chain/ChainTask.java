package eu.okaeri.tasker.core.chain;

import eu.okaeri.tasker.core.Taskerable;
import eu.okaeri.tasker.core.role.DefaultTaskerRunnable;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Delegate;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;

@Data
@Builder
public class ChainTask {
    protected final @Builder.Default @Delegate Taskerable<?> taskerable = new DefaultTaskerRunnable<>(() -> {});
    protected final @Builder.Default Function<TaskerChainAccessor, Boolean> condition = accessor -> true;
    protected final @Builder.Default Consumer<TaskerChainAccessor> callback = accessor -> {};
    protected final @Builder.Default Duration delay = Duration.ZERO;
    protected final @Builder.Default boolean exceptionHandler = false;
}
