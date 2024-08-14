package eu.okaeri.tasker.core.role;

import eu.okaeri.tasker.core.Taskerable;

import java.util.function.Supplier;

public interface TaskerSupplier<T> extends Supplier<T>, Taskerable<T> {
}
