package eu.okaeri.tasker.core.role;

import eu.okaeri.tasker.core.chain.TaskerChainAccessor;
import eu.okaeri.tasker.core.context.DefaultTaskerContext;
import eu.okaeri.tasker.core.context.TaskerContext;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.Delegate;

import java.util.function.Function;
import java.util.function.Supplier;

@AllArgsConstructor
@RequiredArgsConstructor
public class DefaultTaskerFunction<T, R> implements TaskerFunction<T, R> {

    protected @Accessors(fluent = true) @Setter Supplier<TaskerContext> context = DefaultTaskerContext::getInstance;
    protected @Accessors(fluent = true) @Getter @Setter String input = TaskerChainAccessor.DATA_VALUE;
    protected @Accessors(fluent = true) @Getter @Setter String output = TaskerChainAccessor.DATA_VALUE;
    protected final @Delegate Function<T, R> function;

    @Override
    public TaskerContext context() {
        return this.context.get();
    }

    @Override
    public void call(@NonNull TaskerChainAccessor accessor, @NonNull Runnable callback) {
        this.context().run(() -> {
            T oldData = accessor.data(this.input);
            R newData = this.apply(oldData);
            accessor.data(this.output, newData);
            callback.run();
        });
    }
}
