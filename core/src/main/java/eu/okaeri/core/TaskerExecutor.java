package eu.okaeri.core;

public interface TaskerExecutor {

    boolean isMain();

    void run(Runnable runnable, Runnable callback, boolean async);

    default void runSync(Runnable runnable, Runnable callback) {
        this.run(runnable, callback, false);
    }

    default void runAsync(Runnable runnable, Runnable callback) {
        this.run(runnable, callback, true);
    }
}
