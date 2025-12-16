package core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import interfaces.IItem;

public class Repositorio<T extends IItem> {
    private Map<Integer, T> map;

    public Repositorio() {
        this.map = new HashMap<>();
    }

    protected void add(T item) {
        this.map.put(item.getID(), item);
    }

    public T get(int itemID) {
        return this.map.get(itemID);
    }

    public boolean update(int itemID, T item) {
        T temp = this.map.get(itemID);
        if (temp == null) {
            return false;
        }
        temp.update(item);
        this.map.put(itemID, temp);
        return true;
    }

    protected boolean remove(int itemID) {
        T temp = this.map.remove(itemID);
        if (temp == null) {
            return false;
        }
        return true;
    }

    public ArrayList<T> filteredSearch(Predicate<T> comparator) {
        ArrayList<T> list = new ArrayList<>();

        for (T obj : map.values()) {
            if (comparator.test(obj)) {
                list.add(obj);
            }
        }
        return list;
    }

    public ArrayList<T> getAll() {
        return new ArrayList<T>(this.map.values());
    }
}
