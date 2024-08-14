package eu.okaeri.tasker.core.role;

import eu.okaeri.tasker.core.Taskerable;

import java.util.function.Consumer;

public interface TaskerConsumer<T> extends Consumer<T>, Taskerable<T> {
}
