package net.team33.random.test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableMap;

public class GenericData<K, V> {

    private Map<K, List<V>> mapVal;
    private Map<K, BigDecimal> bigDecimalMap;
    private Map<String, Set<V>> stringSetMap;

    public final Map<K, List<V>> getMapVal() {
        return unmodifiableMap(mapVal);
    }

    public final GenericData<K, V> setMapVal(final Map<K, List<V>> mapVal) {
        this.mapVal = new HashMap<>(mapVal);
        return this;
    }

    public final Map<K, BigDecimal> getBigDecimalMap() {
        return unmodifiableMap(bigDecimalMap);
    }

    public final GenericData<K, V> setBigDecimalMap(final Map<K, BigDecimal> bigDecimalMap) {
        this.bigDecimalMap = new HashMap<>(bigDecimalMap);
        return this;
    }

    public final Map<String, Set<V>> getStringSetMap() {
        return unmodifiableMap(stringSetMap);
    }

    public final GenericData<K, V> setStringSetMap(final Map<String, Set<V>> stringSetMap) {
        this.stringSetMap = new HashMap<>(stringSetMap);
        return this;
    }
}
