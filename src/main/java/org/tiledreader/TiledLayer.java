package org.tiledreader;

import java.awt.Color;
import java.util.Collections;
import java.util.Map;

/**
 * <p>A TiledLayer represents a tile layer, object layer, image layer, or group
 * layer, depending on which subclass of TiledLayer it is. The TiledLayer class
 * is responsible for the attributes that all layer types have in common. Since
 * a group layer's opacity, visibility, tint color, rendering offsets, and
 * parallax scrolling factors recursively affect its child layers, a TiledLayer
 * object's methods specify those attributes as relative (to the layer's group
 * layer, if it has one) or as absolute. If the layer is not contained in a
 * group layer, its relative opacity, visibility, etc. are equal to its absolute
 * opacity, visibility, etc.</p>
 * @author Alex Heyman
 */
public abstract class TiledLayer implements TiledCustomizable {
    
    private final String name;
    private final TiledGroupLayer parent;
    private final float relOpacity, absOpacity;
    private final boolean relVisible, absVisible;
    private final Color relTintColor, absTintColor;
    private final float relOffsetX, absOffsetX, relOffsetY, absOffsetY,
            relParallaxX, absParallaxX, relParallaxY, absParallaxY;
    private Map<String,Object> properties = Collections.emptyMap();
    
    TiledLayer(String name, TiledGroupLayer parent, float relOpacity, boolean relVisible,
            Color relTintColor, float relOffsetX, float relOffsetY, float relParallaxX, float relParallaxY) {
        this.name = name;
        this.parent = parent;
        this.relOpacity = relOpacity;
        this.relVisible = relVisible;
        this.relTintColor = relTintColor;
        this.relOffsetX = relOffsetX;
        this.relOffsetY = relOffsetY;
        this.relParallaxX = relParallaxX;
        this.relParallaxY = relParallaxY;
        if (parent == null) {
            absOpacity = relOpacity;
            absVisible = relVisible;
            absTintColor = relTintColor;
            absOffsetX = relOffsetX;
            absOffsetY = relOffsetY;
            absParallaxX = relParallaxX;
            absParallaxY = relParallaxY;
        } else {
            absOpacity = parent.getAbsOpacity() * relOpacity;
            absVisible = (parent.getAbsVisible() && relVisible);
            Color parentTintColor = parent.getRelTintColor();
            int blendedR = (int)Math.round((relTintColor.getRed()*parentTintColor.getRed())/255.0);
            int blendedG = (int)Math.round((relTintColor.getGreen()*parentTintColor.getGreen())/255.0);
            int blendedB = (int)Math.round((relTintColor.getBlue()*parentTintColor.getBlue())/255.0);
            int blendedA = (int)Math.round((relTintColor.getAlpha()*parentTintColor.getAlpha())/255.0);
            absTintColor = new Color(blendedR, blendedG, blendedB, blendedA);
            absOffsetX = parent.getAbsOffsetX() + relOffsetX;
            absOffsetY = parent.getAbsOffsetY() + relOffsetY;
            absParallaxX = parent.getAbsParallaxX() * relParallaxX;
            absParallaxY = parent.getAbsParallaxY() * relParallaxY;
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
     * Returns this layer's tint color, relative to its group layer if it has
     * one, or #FFFFFFFF (white) if none was specified.
     * @return This layer's relative tint color
     */
    public final Color getRelTintColor() {
        return relTintColor;
    }
    
    /**
     * Returns this layer's absolute tint color, or #FFFFFFFF (white) if none
     * was specified.
     * @return This layer's absolute tint color
     */
    public final Color getAbsTintColor() {
        return absTintColor;
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
     * Returns this layer's horizontal parallax scrolling factor, relative to
     * its group layer if it has one.
     * @return This layer's relative horizontal parallax scrolling factor
     */
    public final float getRelParallaxX() {
        return relParallaxX;
    }
    
    /**
     * Returns this layer's absolute horizontal parallax scrolling factor.
     * @return This layer's absolute horizontal parallax scrolling factor
     */
    public final float getAbsParallaxX() {
        return absParallaxX;
    }
    
    /**
     * Returns this layer's vertical parallax scrolling factor, relative to its
     * group layer if it has one.
     * @return This layer's relative vertical parallax scrolling factor
     */
    public final float getRelParallaxY() {
        return relParallaxY;
    }
    
    /**
     * Returns this layer's absolute vertical parallax scrolling factor.
     * @return This layer's absolute vertical parallax scrolling factor
     */
    public final float getAbsParallaxY() {
        return absParallaxY;
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
