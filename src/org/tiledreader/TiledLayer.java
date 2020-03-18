package org.tiledreader;

import java.util.Collections;
import java.util.Map;

/**
 * <p>A TiledLayer represents a tile layer, object layer, image layer, or group
 * layer, depending on which subclass of TiledLayer it is. The TiledLayer class
 * is responsible for the attributes that all layer types have in common. Since
 * a group layer's opacity, visibility, and rendering x and y offsets
 * recursively affect its child layers, a TiledLayer object's methods specify
 * those four attributes as relative (to the layer's group layer, if it has one)
 * or as absolute. If the layer is not contained in a group layer, its relative
 * opacity, visibility, etc. are equal to its absolute opacity, visibility, etc.
 * </p>
 * @author Alex Heyman
 */
public abstract class TiledLayer {
    
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
    
    /**
     * Returns this layer's name.
     * @return This layer's name
     */
    public final String getName() {
        return name;
    }
    
    /**
     * Returns the group layer that this layer is a child of, or null if this
     * layer is not contained in a group layer.
     * @return The group layer that this layer is a child of
     */
    public final TiledGroupLayer getParent() {
        return parent;
    }
    
    /**
     * Returns this layer's opacity, relative to its group layer if it has one.
     * @return This layer's relative opacity
     */
    public final float getRelOpacity() {
        return relOpacity;
    }
    
    /**
     * Returns this layer's absolute opacity.
     * @return This layer's absolute opacity
     */
    public final float getAbsOpacity() {
        return absOpacity;
    }
    
    /**
     * Returns whether this layer is visible, relative to its group layer if it
     * has one.
     * @return This layer's relative visibility
     */
    public final boolean getRelVisible() {
        return relVisible;
    }
    
    /**
     * Returns whether this layer is visible.
     * @return This layer's absolute visibility
     */
    public final boolean getAbsVisible() {
        return absVisible;
    }
    
    /**
     * Returns this layer's rendering x offset, relative to its group layer if
     * it has one.
     * @return This layer's relative rendering x offset
     */
    public final float getRelOffsetX() {
        return relOffsetX;
    }
    
    /**
     * Returns this layer's absolute rendering x offset.
     * @return This layer's absolute rendering x offset
     */
    public final float getAbsOffsetX() {
        return absOffsetX;
    }
    
    /**
     * Returns this layer's rendering y offset, relative to its group layer if
     * it has one.
     * @return This layer's relative rendering y offset
     */
    public final float getRelOffsetY() {
        return relOffsetY;
    }
    
    /**
     * Returns this layer's absolute rendering y offset.
     * @return This layer's absolute rendering y offset
     */
    public final float getAbsOffsetY() {
        return absOffsetY;
    }
    
    /**
     * Returns an unmodifiable Map view of this layer's custom properties. Each
     * key in the Map is the name of a property, and its corresponding value is
     * the value of that property. The type of the value object corresponds
     * to the type of the property: String for a string property, Integer for an
     * int, Float for a float, Boolean for a bool, <code>java.awt.Color</code>
     * for a color, and <code>java.io.File</code> for a file.
     * @return This layer's custom properties
     */
    public final Map<String,Object> getProperties() {
        return properties;
    }
    
    /**
     * Returns the value of this layer's custom property with the specified
     * name, or null if no such property was specified. The type of the returned
     * value corresponds to the type of the property: String for a string
     * property, Integer for an int, Float for a float, Boolean for a bool,
     * <code>java.awt.Color</code> for a color, and <code>java.io.File</code>
     * for a file.
     * @param name The name of the property whose value is to be returned
     * @return The value of this layer's custom property with the specified name
     */
    public final Object getProperty(String name) {
        return properties.get(name);
    }
    
}
