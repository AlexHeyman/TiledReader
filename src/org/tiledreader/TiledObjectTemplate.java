package org.tiledreader;

import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Alex Heyman
 */
public class TiledObjectTemplate {
    
    private final String name, type;
    private final float width, height, rotation;
    private final TiledTile tile;
    private final boolean visible;
    private final TiledObject.Shape shape;
    private final List<Point2D> points;
    private final TiledText text;
    private final Map<String,Object> properties;
    
    TiledObjectTemplate(String name, String type, float width, float height, float rotation, TiledTile tile,
            boolean visible, TiledObject.Shape shape, List<Point2D> points, TiledText text,
            Map<String,Object> properties) {
        this.name = name;
        this.type = type;
        this.width = width;
        this.height = height;
        this.rotation = rotation;
        this.tile = tile;
        this.visible = visible;
        this.shape = shape;
        this.points = (points == null ? Collections.emptyList() : Collections.unmodifiableList(points));
        this.text = text;
        this.properties = (properties == null ?
                Collections.emptyMap() : Collections.unmodifiableMap(properties));
    }
    
    public final String getName() {
        return name;
    }
    
    public final String getType() {
        return type;
    }
    
    public final float getWidth() {
        return width;
    }
    
    public final float getHeight() {
        return height;
    }
    
    public final float getRotation() {
        return rotation;
    }
    
    /**
     * Returns the tile that represents objects with this template, if this
     * template is for a tile object. Returns null otherwise.
     * @return The tile that represents objects with this template
     */
    public final TiledTile getTile() {
        return tile;
    }
    
    public final boolean getVisible() {
        return visible;
    }
    
    public final TiledObject.Shape getShape() {
        return shape;
    }
    
    /**
     * Returns an unmodifiable List view of the sequence of vertex (x, y)
     * coordinates of objects with this template, if this template is for a
     * polygon or polyline. Returns an empty unmodifiable List otherwise. The
     * coordinates are relative to the (x, y) positions of objects with this
     * template.
     * @return The vertex (x, y) coordinates of objects with this template
     */
    public final List<Point2D> getPoints() {
        return points;
    }
    
    /**
     * Returns the text of objects with this template, if this template is for a
     * text object. Returns null otherwise.
     * @return The text of objects with this template
     */
    public final TiledText getText() {
        return text;
    }
    
    public final Map<String,Object> getProperties() {
        return properties;
    }
    
    public final Object getProperty(String name) {
        return properties.get(name);
    }
    
}
