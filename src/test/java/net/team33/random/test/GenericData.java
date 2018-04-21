package net.team33.random.test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenericData<K, V> {

    private Map<K, List<V>> mapVal;

    public final Map<K, List<V>> getMapVal() {
        return Collections.unmodifiableMap(mapVal);
    }

    public final GenericData<K, V> setMapVal(final Map<K, List<V>> mapVal) {
        this.mapVal = new HashMap<>(mapVal);
        return this;
    }
}
