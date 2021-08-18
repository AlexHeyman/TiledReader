package org.tiledreader;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * <p>A TiledObject represents a Tiled object in an object layer, or as part of
 * the specification of a tile's collision properties. It corresponds to an
 * &lt;object&gt; tag inside an &lt;objectgroup&gt; tag in a Tiled XML file.</p>
 * @author Alex Heyman
 */
public class TiledObject implements TiledCustomizable {
    
    /**
     * <p>Represents a shape that a Tiled object can take, such as an ellipse or
     * polygon.</p>
     */
    public static enum Shape {
        ELLIPSE, POINT, POLYGON, POLYLINE, RECTANGLE, TEXT
    }
    
    private final int id;
    private final String name, type;
    private final TiledObjectType typeInfo;
    private final float x, y, width, height, rotation;
    TiledTile tile;
    private final int tileFlags;
    private final boolean visible;
    private final TiledObject.Shape shape;
    private final List<Point2D> points;
    private final TiledText text;
    private final Map<String,Object> nonDefaultProperties, properties;
    private final TiledObjectTemplate template;
    
    TiledObject(int id, String name, String type, TiledObjectType typeInfo, float x, float y,
            float width, float height, float rotation, TiledTile tile, int tileFlags, boolean visible,
            TiledObject.Shape shape, List<Point2D> points, TiledText text,
            Map<String,Object> nonDefaultProperties, TiledObjectTemplate template) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.typeInfo = typeInfo;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.rotation = rotation;
        this.tile = tile;
        this.tileFlags = tileFlags;
        this.visible = visible;
        this.shape = shape;
        this.points = (points == null ? Collections.emptyList() : Collections.unmodifiableList(points));
        this.text = text;
        this.nonDefaultProperties = (nonDefaultProperties == null ?
                Collections.emptyMap() : Collections.unmodifiableMap(nonDefaultProperties));
        
        List<Map<String,Object>> propertyTiers = new ArrayList<>();
        if (!this.nonDefaultProperties.isEmpty()) {
            propertyTiers.add(this.nonDefaultProperties);
        }
        if (template != null && !template.getProperties().isEmpty()) {
            propertyTiers.add(template.getProperties());
        }
        if (tile != null && !tile.getNonDefaultProperties().isEmpty()) {
            propertyTiers.add(tile.getNonDefaultProperties());
        }
        if (typeInfo != null && !typeInfo.getProperties().isEmpty()) {
            propertyTiers.add(typeInfo.getProperties());
        }
        properties = new TieredMap<>(propertyTiers);
        
        this.template = template;
    }
    
    /**
     * Returns this object's ID.
     * @return This object's ID
     */
    public int getID() {
        return id;
    }
    
    /**
     * Returns this object's name (the empty string by default).
     * @return This object's name
     */
    public final String getName() {
        return name;
    }
    
    /**
     * Returns this object's type (the empty string by default).
     * @return This object's type
     */
    public final String getType() {
        return type;
    }
    
    /**
     * Returns the object type information that determined the default values of
     * this object's custom properties, or null if no such information was used.
     * @return The object type information that determined the default values of
     * this object's custom properties
     */
    public final TiledObjectType getTypeInfo() {
        return typeInfo;
    }
    
    /**
     * Returns this object's x-coordinate in pixels.
     * @return This object's x-coordinate in pixels
     */
    public final float getX() {
        return x;
    }
    
    /**
     * Returns this object's y-coordinate in pixels.
     * @return This object's y-coordinate in pixels
     */
    public final float getY() {
        return y;
    }
    
    /**
     * Returns this object's width in pixels (0 by default).
     * @return This object's width in pixels
     */
    public final float getWidth() {
        return width;
    }
    
    /**
     * Returns this object's height in pixels (0 by default).
     * @return This object's height in pixels
     */
    public final float getHeight() {
        return height;
    }
    
    /**
     * Returns the rotation of this object in clockwise degrees (0 by default).
     * @return The rotation of this object in clockwise degrees
     */
    public final float getRotation() {
        return rotation;
    }
    
    /**
     * Returns the tile that represents this object, if it is a tile object.
     * Returns null otherwise.
     * @return The tile that represents this object
     */
    public final TiledTile getTile() {
        return tile;
    }
    
    /**
     * Returns whether the tile that represents this object is flipped
     * horizontally, if this object is a tile object. Returns false otherwise.
     * @return Whether this object's tile is flipped horizontally
     */
    public final boolean getTileXFlip() {
        return (tileFlags & TiledTileLayer.FL_FLIPX) != 0;
    }
    
    /**
     * Returns whether the tile that represents this object is flipped
     * vertically, if this object is a tile object. Returns false otherwise.
     * @return Whether this object's tile is flipped vertically
     */
    public final boolean getTileYFlip() {
        return (tileFlags & TiledTileLayer.FL_FLIPY) != 0;
    }
    
    /**
     * Returns whether the tile that represents this object is flipped
     * diagonally, if this object is a tile object. Returns false otherwise.
     * @return Whether this object's tile is flipped diagonally
     */
    public final boolean getTileDFlip() {
        return (tileFlags & TiledTileLayer.FL_FLIPD) != 0;
    }
    
    /**
     * Returns whether this object is marked as visible (true by default).
     * @return Whether this object is marked as visible
     */
    public final boolean getVisible() {
        return visible;
    }
    
    /**
     * Returns this object's shape (<code>TiledObject.Shape.RECTANGLE</code> by
     * default).
     * @return This object's shape
     */
    public final TiledObject.Shape getShape() {
        return shape;
    }
    
    /**
     * Returns an unmodifiable List view of this sequence of vertex (x, y)
     * coordinates of this object, if it is a polygon or polyline. Returns an
     * empty unmodifiable List otherwise. The coordinates are relative to this
     * object's (x, y) position.
     * @return The vertex (x, y) coordinates of this object
     */
    public final List<Point2D> getPoints() {
        return points;
    }
    
    /**
     * Returns the text of this object, if it is a text object. Returns null
     * otherwise.
     * @return This object's text
     */
    public final TiledText getText() {
        return text;
    }
    
    @Override
    public final Map<String,Object> getProperties() {
        return properties;
    }
    
    @Override
    public final Object getProperty(String name) {
        return properties.get(name);
    }
    
    /**
     * Returns an unmodifiable Map view of this object's custom properties that
     * were specified by the object itself, rather than as defaults via its
     * template, tile, or object type. The format of the returned Map is the
     * same as the format of the Map returned by getProperties().
     * @return This object's non-default custom properties
     */
    public final Map<String,Object> getNonDefaultProperties() {
        return nonDefaultProperties;
    }
    
    /**
     * Returns the template from which this object inherits its default
     * properties, or null if no template was specified.
     * @return This object's template
     */
    public final TiledObjectTemplate getTemplate() {
        return template;
    }
    
}
