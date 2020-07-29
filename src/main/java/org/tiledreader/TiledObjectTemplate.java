package org.tiledreader;

import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * <p>A TiledObjectTemplate represents a template from which TiledObjects can
 * borrow their default properties. It corresponds to a &lt;template&gt; tag in
 * a Tiled XML file.</p>
 * @author Alex Heyman
 */
public class TiledObjectTemplate extends TiledResource {
    
    private final String name, type;
    private final float width, height, rotation;
    private final TiledTile tile;
    private final int tileFlags;
    private final boolean visible;
    private final TiledObject.Shape shape;
    private final List<Point2D> points;
    private final TiledText text;
    private final Map<String,Object> properties;
    
    TiledObjectTemplate(String path, String name, String type, float width, float height, float rotation,
            TiledTile tile, int tileFlags, boolean visible, TiledObject.Shape shape, List<Point2D> points,
            TiledText text, Map<String,Object> properties) {
        super(path);
        this.name = name;
        this.type = type;
        this.width = width;
        this.height = height;
        this.rotation = rotation;
        this.tile = tile;
        this.tileFlags = tileFlags;
        this.visible = visible;
        this.shape = shape;
        this.points = (points == null ? Collections.emptyList() : Collections.unmodifiableList(points));
        this.text = text;
        this.properties = (properties == null ?
                Collections.emptyMap() : Collections.unmodifiableMap(properties));
    }
    
    /**
     * Returns the name of objects with this template (the empty string by
     * default).
     * @return The name of objects with this template
     */
    public final String getName() {
        return name;
    }
    
    /**
     * Returns the type of objects with this template (the empty string by
     * default).
     * @return The type of objects with this template
     */
    public final String getType() {
        return type;
    }
    
    /**
     * Returns the width in pixels of objects with this template (0 by default).
     * @return The width in pixels of objects with this template
     */
    public final float getWidth() {
        return width;
    }
    
    /**
     * Returns the height in pixels of objects with this template (0 by
     * default).
     * @return The height in pixels of objects with this template
     */
    public final float getHeight() {
        return height;
    }
    
    /**
     * Returns the rotation in clockwise degrees of objects with this template
     * (0 by default).
     * @return The rotation in clockwise degrees of objects with this template
     */
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
    
    /**
     * Returns whether the the tile that represents objects with this template
     * is flipped horizontally, if this template is for a tile object. Returns
     * false otherwise.
     * @return Whether this template's tile is flipped horizontally
     */
    public final boolean getTileXFlip() {
        return (tileFlags & TiledTileLayer.FL_FLIPX) != 0;
    }
    
    /**
     * Returns whether the the tile that represents objects with this template
     * is flipped vertically, if this template is for a tile object. Returns
     * false otherwise.
     * @return Whether this template's tile is flipped vertically
     */
    public final boolean getTileYFlip() {
        return (tileFlags & TiledTileLayer.FL_FLIPY) != 0;
    }
    
    /**
     * Returns whether the the tile that represents objects with this template
     * is flipped diagonally, if this template is for a tile object. Returns
     * false otherwise.
     * @return Whether this template's tile is flipped diagonally
     */
    public final boolean getTileDFlip() {
        return (tileFlags & TiledTileLayer.FL_FLIPD) != 0;
    }
    
    /**
     * Returns whether objects with this template are marked as visible (true by
     * default).
     * @return Whether objects with this template are marked as visible
     */
    public final boolean getVisible() {
        return visible;
    }
    
    /**
     * Returns the shape of objects with this template
     * (<code>TiledObject.Shape.RECTANGLE</code> by default).
     * @return The shape of objects with this template
     */
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
    
    /**
     * Returns an unmodifiable Map view of the custom properties of objects with
     * this template. Each key in the Map is the name of a property, and its
     * corresponding value is the value of that property. The type of the value
     * object corresponds to the type of the property: String for a string
     * property, Integer for an int, Float for a float, Boolean for a bool,
     * <code>java.awt.Color</code> for a color, <code>java.io.File</code> for a
     * file, and TiledObject for an object (unless the object property is unset,
     * in which case the value is null).
     * @return The custom properties of objects with this template
     */
    public final Map<String,Object> getProperties() {
        return properties;
    }
    
    /**
     * Returns the value of the custom property with the specified name of
     * objects with this template, or null if no such property was specified.
     * The type of the returned value object corresponds to the type of the
     * property: String for a string property, Integer for an int, Float for a
     * float, Boolean for a bool, <code>java.awt.Color</code> for a color,
     * <code>java.io.File</code> for a file, and TiledObject for an object
     * (unless the object property is unset, in which case the value is null).
     * @param name The name of the property whose value is to be returned
     * @return The value of the custom property with the specified name of
     * objects with this template
     */
    public final Object getProperty(String name) {
        return properties.get(name);
    }
    
}
