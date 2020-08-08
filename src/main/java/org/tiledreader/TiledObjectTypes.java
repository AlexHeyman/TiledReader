package org.tiledreader;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * <p>A TiledObjectTypes object represents a set of object types read from a
 * Tiled object types XML file. It corresponds to an &lt;objecttypes&gt; tag in
 * such a file. A TiledObjectTypes object can be used as an unmodifiable Map
 * with object type names as keys and the object types themselves as values.</p>
 * @author Alex Heyman
 */
public class TiledObjectTypes implements Map<String,TiledObjectType> {
    
    private final Map<String,TiledObjectType> objectTypes;
    
    TiledObjectTypes(Map<String,TiledObjectType> objectTypes) {
        this.objectTypes = Collections.unmodifiableMap(objectTypes);
    }
    
    @Override
    public final int size() {
        return objectTypes.size();
    }
    
    @Override
    public final boolean isEmpty() {
        return objectTypes.isEmpty();
    }
    
    @Override
    public final boolean containsKey(Object o) {
        return objectTypes.containsKey(o);
    }
    
    @Override
    public final boolean containsValue(Object o) {
        return objectTypes.containsValue(o);
    }
    
    @Override
    public final TiledObjectType get(Object o) {
        return objectTypes.get(o);
    }
    
    @Override
    public final TiledObjectType put(String k, TiledObjectType v) {
        return objectTypes.put(k, v);
    }
    
    @Override
    public final TiledObjectType remove(Object o) {
        return objectTypes.remove(o);
    }
    
    @Override
    public final void putAll(Map<? extends String, ? extends TiledObjectType> map) {
        objectTypes.putAll(map);
    }
    
    @Override
    public final void clear() {
        objectTypes.clear();
    }
    
    @Override
    public final Set<String> keySet() {
        return objectTypes.keySet();
    }
    
    @Override
    public final Collection<TiledObjectType> values() {
        return objectTypes.values();
    }
    
    @Override
    public final Set<Entry<String,TiledObjectType>> entrySet() {
        return objectTypes.entrySet();
    }
    
}
