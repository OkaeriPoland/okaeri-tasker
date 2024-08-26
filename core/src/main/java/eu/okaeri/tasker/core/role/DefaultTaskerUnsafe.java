package eu.okaeri.tasker.core.role;

import eu.okaeri.tasker.core.chain.TaskerChainAccessor;
import eu.okaeri.tasker.core.context.TaskerContext;
import lombok.NonNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class DefaultTaskerUnsafe extends DefaultTaskerConsumer<TaskerChainAccessor> {

    public DefaultTaskerUnsafe(Supplier<TaskerContext> context, String input, String output, Consumer<TaskerChainAccessor> consumer) {
        super(context, input, output, consumer);
    }

    public DefaultTaskerUnsafe(Consumer<TaskerChainAccessor> consumer) {
        super(consumer);
    }

    @Override
    public Runnable call(@NonNull TaskerChainAccessor accessor) {
        return () -> super.consumer.accept(accessor);
    }
}
