package eu.okaeri.tasker.core.role;

import eu.okaeri.tasker.core.Taskerable;

public interface TaskerRunnable<T> extends Runnable, Taskerable<T> {
}
