package operatedarocket.util.HashMaps;

import java.util.HashMap;

public class HashMapBuilder<K, V> {
    private HashMap<K, V> res;

    public HashMapBuilder() {
        res = new HashMap<>();
    }
    public HashMapBuilder(HashMap<K, V> parent) {
        res = new HashMap<>(parent);
    }

    public HashMapBuilder<K, V> put(K key, V value) {
        res.put(key, value);
        return this;
    }

    public HashMap<K, V> build() {
        return res;
    }
}
