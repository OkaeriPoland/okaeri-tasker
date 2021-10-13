package eu.okaeri.tasker.core;

public interface TaskerExecutor {

    boolean isMain();

    void schedule(Runnable runnable, boolean async);

    void run(Runnable runnable, Runnable callback, boolean async);
}
