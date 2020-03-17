package org.tiledreader;

import java.util.Collections;
import java.util.Map;

/**
 *
 * @author Alex Heyman
 */
public class TiledLayer {
    
    private final String name;
    private final TiledGroupLayer parent;
    private final float relOpacity, absOpacity;
    private final boolean relVisible, absVisible;
    private final float relOffsetX, absOffsetX, relOffsetY, absOffsetY;
    private Map<String,Object> properties = Collections.emptyMap();
    
    TiledLayer(String name, TiledGroupLayer parent, float relOpacity, boolean relVisible,
            float relOffsetX, float relOffsetY) {
        this.name = name;
        this.parent = parent;
        this.relOpacity = relOpacity;
        this.relVisible = relVisible;
        this.relOffsetX = relOffsetX;
        this.relOffsetY = relOffsetY;
        if (parent == null) {
            absOpacity = relOpacity;
            absVisible = relVisible;
            absOffsetX = relOffsetX;
            absOffsetY = relOffsetY;
        } else {
            absOpacity = parent.getAbsOpacity() * relOpacity;
            absVisible = (parent.getAbsVisible() && relVisible);
            absOffsetX = parent.getAbsOffsetX() + relOffsetX;
            absOffsetY = parent.getAbsOffsetY() + relOffsetY;
        }
    }
    
    final void setProperties(Map<String,Object> properties) {
        if (properties != null) {
            this.properties = Collections.unmodifiableMap(properties);
        }
    }
    
    public final String getName() {
        return name;
    }
    
    /**
     * Returns the group layer that this layer is a child of, or null if this
     * layer is not in a group layer.
     * @return The group layer that this layer is a child of
     */
    public final TiledGroupLayer getParent() {
        return parent;
    }
    
    public final float getRelOpacity() {
        return relOpacity;
    }
    
    public final float getAbsOpacity() {
        return absOpacity;
    }
    
    public final boolean getRelVisible() {
        return relVisible;
    }
    
    public final boolean getAbsVisible() {
        return absVisible;
    }
    
    public final float getRelOffsetX() {
        return relOffsetX;
    }
    
    public final float getAbsOffsetX() {
        return absOffsetX;
    }
    
    public final float getRelOffsetY() {
        return relOffsetY;
    }
    
    public final float getAbsOffsetY() {
        return absOffsetY;
    }
    
    public final Map<String,Object> getProperties() {
        return properties;
    }
    
    public final Object getProperty(String name) {
        return properties.get(name);
    }
    
}
