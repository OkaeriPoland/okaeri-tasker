package eu.okaeri.tasker.core.role;

import eu.okaeri.tasker.core.chain.TaskerChainAccessor;
import eu.okaeri.tasker.core.context.DefaultTaskerContext;
import eu.okaeri.tasker.core.context.TaskerContext;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.Delegate;

import java.util.function.Supplier;

@AllArgsConstructor
@RequiredArgsConstructor
public class DefaultTaskerSupplier<T> implements TaskerSupplier<T> {

    protected @Accessors(fluent = true) @Setter Supplier<TaskerContext> context = DefaultTaskerContext::getInstance;
    protected @Accessors(fluent = true) @Getter @Setter String input = TaskerChainAccessor.DATA_VALUE;
    protected @Accessors(fluent = true) @Getter @Setter String output = TaskerChainAccessor.DATA_VALUE;
    protected final @Delegate Supplier<T> supplier;

    @Override
    public TaskerContext context() {
        return this.context.get();
    }

    @Override
    public Runnable call(@NonNull TaskerChainAccessor accessor) {
        return () -> accessor.data(this.output, this.get());
    }
}
