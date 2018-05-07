package utils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class FIFOCache {
    public static <T> Set<T> newSet(int capacity) {
        return Collections.newSetFromMap(new LinkedHashMap<T, Boolean>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<T, Boolean> eldest) {
                return size() > capacity;
            }
        });
    }
}
