package eu.okaeri.tasker.core;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

@RequiredArgsConstructor
public class TaskerableWrapper<T> implements Taskerable<T> {
    protected final @Delegate Taskerable<T> taskerable;
}
