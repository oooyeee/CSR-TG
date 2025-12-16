package util;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

// thread unsafe simple event emitter
public class EventEmitter<S, D> {
    private final Map<String, List<BiConsumer<S, D>>> listeners = new HashMap<>();

    public void on(String event, BiConsumer<S, D> listener) { // adds to last
        listeners.computeIfAbsent(event, k -> new ArrayList<>()).add(listener);
    }

    public void off(String event, BiConsumer<S, D> listener) {
        List<BiConsumer<S, D>> list = listeners.get(event);
        if (list != null) {
            list.remove(listener); // removes first (FIFO)
            if (list.isEmpty()) {
                listeners.remove(event);
            }
        }
    }

    public void emit(String event, S sessionObj, D dataObj) {
        List<BiConsumer<S, D>> list = listeners.get(event);
        if (list != null) {
            for (BiConsumer<S, D> listener : list) {
                listener.accept(sessionObj, dataObj);
            }
        }
    }

    public boolean hasEvent(String event){
        return this.listeners.containsKey(event);
    }
}