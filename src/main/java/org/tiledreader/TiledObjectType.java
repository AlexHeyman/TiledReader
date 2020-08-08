package org.tiledreader;

import java.awt.Color;
import java.util.Collections;
import java.util.Map;

/**
 * <p>A TiledObjectType represents a single object type read from a Tiled object
 * types XML file. It corresponds to an &lt;objecttype&gt; tag in such a file.
 * </p>
 * @author Alex Heyman
 */
public class TiledObjectType implements TiledCustomizable {
    
    private final String name;
    private final Color color;
    private final Map<String,Object> properties;
    
    TiledObjectType(String name, Color color, Map<String,Object> properties) {
        this.name = name;
        this.color = color;
        this.properties = Collections.unmodifiableMap(properties);
    }
    
    /**
     * Returns this object type's name.
     * @return This object type's name
     */
    public final String getName() {
        return name;
    }
    
    /**
     * Returns the color used to display objects of this type in the Tiled
     * editor.
     * @return This object type's associated color
     */
    public final Color getColor() {
        return color;
    }
    
    @Override
    public final Map<String,Object> getProperties() {
        return properties;
    }
    
    @Override
    public final Object getProperty(String name) {
        return properties.get(name);
    }
    
}
