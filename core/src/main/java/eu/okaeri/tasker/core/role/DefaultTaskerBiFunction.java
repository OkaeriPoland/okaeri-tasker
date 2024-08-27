package eu.okaeri.tasker.core.role;

import eu.okaeri.tasker.core.chain.TaskerChainAccessor;
import eu.okaeri.tasker.core.context.DefaultTaskerContext;
import eu.okaeri.tasker.core.context.TaskerContext;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.Delegate;

import java.util.function.BiFunction;
import java.util.function.Supplier;

@AllArgsConstructor
@RequiredArgsConstructor
public class DefaultTaskerBiFunction<T, S, R> implements TaskerBiFunction<T, S, R> {

    protected @Accessors(fluent = true) @Setter Supplier<TaskerContext> context = DefaultTaskerContext::getInstance;
    protected @Accessors(fluent = true) @Getter @Setter String input = TaskerChainAccessor.DATA_VALUE;
    protected @Accessors(fluent = true) @Getter @Setter String inputTwo = TaskerChainAccessor.DATA_VALUE_2;
    protected @Accessors(fluent = true) @Getter @Setter String output = TaskerChainAccessor.DATA_VALUE;
    protected final @Delegate BiFunction<T, S, R> function;

    @Override
    public TaskerContext context() {
        return this.context.get();
    }

    @Override
    public Runnable call(@NonNull TaskerChainAccessor accessor) {
        return () -> {
            T oldData = accessor.data(this.input);
            S dataTwo = accessor.data(this.inputTwo);
            R newData = this.apply(oldData, dataTwo);
            accessor.data(this.output, newData);
        };
    }
}
