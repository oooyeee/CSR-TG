package util;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

// thread unsafe simple event emitter
public class EventEmitter<T> {
    private final Map<String, List<Consumer<T>>> listeners = new HashMap<>();

    public void on(String event, Consumer<T> listener) {
        listeners.computeIfAbsent(event, k -> new ArrayList<>()).add(listener);
    }

    public void off(String event, Consumer<T> listener) {
        List<Consumer<T>> list = listeners.get(event);
        if (list != null) {
            list.remove(listener);
            if (list.isEmpty()) {
                listeners.remove(event);
            }
        }
    }

    public void emit(String event, T data) {
        List<Consumer<T>> list = listeners.get(event);
        if (list != null) {
            for (Consumer<T> listener : list) {
                listener.accept(data);
            }
        }
    }
}