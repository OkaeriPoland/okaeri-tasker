package eu.okaeri.tasker.core.chain;

import eu.okaeri.tasker.core.Taskerable;
import eu.okaeri.tasker.core.context.TaskerContext;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class TaskerChainAccessor {

    public static final String DATA_VALUE = "$$__value__$$";
    public static final String DATA_EXCEPTION = "$$__exception__$$";

    private final Map<String, Object> data = new HashMap<>();
    private final TaskerChain<?> chain;

    public void abort(boolean abort) {
        this.chain.abort = abort;
    }

    public boolean abort() {
        return this.chain.abort;
    }

    public void data(@NonNull String key, Object newValue) {
        this.data.put(key, newValue);
    }

    public void data(Object newValue) {
        this.data(DATA_VALUE, newValue);
    }

    public <D> D data(@NonNull String key) {
        return (D) this.data.get(key);
    }

    public <D> D data() {
        return this.data(DATA_VALUE);
    }

    public <D> D dataOr(@NonNull String key, D other) {
        D data = this.data();
        return data == null ? other : data;
    }

    public <D> D dataOr(D other) {
        return this.dataOr(DATA_VALUE, other);
    }

    public boolean has(@NonNull String key) {
        return this.data.containsKey(key);
    }

    public Object remove(@NonNull String key) {
        return this.data.remove(key);
    }

    public TaskerContext lastContext() {
        return this.chain.lastContext;
    }

    public void insert(@NonNull Taskerable<?> taskerable) {
        this.chain.tasks.add(this.chain.currentTaskIndex + 1, ChainTask.builder().taskerable(taskerable).build());
    }

    public void delay(@NonNull Duration duration) {
        this.chain.tasks.add(this.chain.currentTaskIndex + 1, ChainTask.builder().delay(duration).build());
    }
}
