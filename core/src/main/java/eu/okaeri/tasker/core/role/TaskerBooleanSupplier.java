package eu.okaeri.tasker.core.role;

import eu.okaeri.tasker.core.Taskerable;

import java.util.function.BooleanSupplier;

public interface TaskerBooleanSupplier extends BooleanSupplier, Taskerable<Boolean> {
}
