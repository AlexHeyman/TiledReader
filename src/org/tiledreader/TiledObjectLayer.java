package org.tiledreader;

import java.awt.Color;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Alex Heyman
 */
public class TiledObjectLayer extends TiledLayer {
    
    private final Color color;
    private final List<TiledObject> objects;
    
    TiledObjectLayer(String name, TiledGroupLayer parent, float relOpacity, boolean relVisible,
            float relOffsetX, float relOffsetY, Color color, List<TiledObject> objects) {
        super(name, parent, relOpacity, relVisible, relOffsetX, relOffsetY);
        this.color = color;
        this.objects = Collections.unmodifiableList(objects);
    }
    
    /**
     * Returns the color used to display the objects in this group in Tiled, or
     * null if none was specified.
     * @return The color used to display the objects in this group
     */
    public final Color getColor() {
        return color;
    }
    
    /**
     * Returns an unmodifiable List view of the objects in this group.
     * @return The objects in this group
     */
    public final List<TiledObject> getObjects() {
        return objects;
    }
    
}
