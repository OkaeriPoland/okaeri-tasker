package eu.okaeri.tasker.core.role;

import eu.okaeri.tasker.core.Taskerable;

import java.util.function.Predicate;

public interface TaskerPredicate<T> extends Predicate<T>, Taskerable<Boolean> {
}
