package eu.okaeri.tasker.core;

public interface TaskerExecutor<T> {

    boolean isMain();

    T schedule(Runnable runnable, boolean async);

    T run(Runnable runnable, Runnable callback, boolean async);
}
