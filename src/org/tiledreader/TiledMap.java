package org.tiledreader;

import java.awt.Color;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * <p>A TiledMap represents an entire map made with Tiled. It corresponds to a
 * &lt;map&gt; tag in a Tiled XML file.</p>
 * @author Alex Heyman
 */
public class TiledMap extends TiledResource {
    
    /**
     * <p>Represents an orientation that a Tiled map can have.</p>
     */
    public static enum Orientation {
        ORTHOGONAL, ISOMETRIC, STAGGERED, HEXAGONAL
    }
    
    /**
     * <p>Represents an order in which the tiles in a Tiled map's tile layers
     * can be rendered.</p>
     */
    public static enum RenderOrder {
        RIGHT_DOWN, RIGHT_UP, LEFT_DOWN, LEFT_UP
    }
    
    /**
     * <p>Represents an axis that can be staggered in a Tiled map with a
     * staggered or hexagonal orientation.</p>
     */
    public static enum StaggerAxis {
        X, Y
    }
    
    /**
     * <p>Represents a category of indices that can be shifted in a Tiled map
     * with a staggered or hexagonal orientation.</p>
     */
    public static enum StaggerIndex {
        EVEN, ODD
    }
    
    private final Orientation orientation;
    private final RenderOrder renderOrder;
    private final int width, height, tileWidth, tileHeight, hexSideLength;
    private final StaggerAxis staggerAxis;
    private final StaggerIndex staggerIndex;
    private final Color backgroundColor;
    private final List<TiledTileset> tilesets;
    private final List<TiledLayer> topLevelLayers, nonGroupLayers;
    private final Map<String,Object> properties;
    
    TiledMap(String path, Orientation orientation, RenderOrder renderOrder,
            int width, int height, int tileWidth, int tileHeight, int hexSideLength,
            StaggerAxis staggerAxis, StaggerIndex staggerIndex, Color backgroundColor,
            List<TiledTileset> tilesets, List<TiledLayer> topLevelLayers, List<TiledLayer> nonGroupLayers,
            Map<String,Object> properties) {
        super(path);
        this.orientation = orientation;
        this.renderOrder = renderOrder;
        this.width = width;
        this.height = height;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.hexSideLength = hexSideLength;
        this.staggerAxis = staggerAxis;
        this.staggerIndex = staggerIndex;
        this.backgroundColor = backgroundColor;
        this.tilesets = Collections.unmodifiableList(tilesets);
        this.topLevelLayers = Collections.unmodifiableList(topLevelLayers);
        this.nonGroupLayers = Collections.unmodifiableList(nonGroupLayers);
        this.properties = (properties == null ?
                Collections.emptyMap() : Collections.unmodifiableMap(properties));
    }
    
    /**
     * Returns this map's orientation.
     * @return This map's orientation
     */
    public final Orientation getOrientation() {
        return orientation;
    }
    
    /**
     * Returns the order in which the tiles in this map's tile layers are
     * rendered.
     * @return This map's tile rendering order
     */
    public final RenderOrder getRenderOrder() {
        return renderOrder;
    }
    
    /**
     * Returns this map's width in tiles. If this map is marked as infinite, its
     * width is meaningless.
     * @return This map's width in tiles
     */
    public final int getWidth() {
        return width;
    }
    
    /**
     * Returns this map's height in tiles. If this map is marked as infinite,
     * its height is meaningless.
     * @return This map's height in tiles
     */
    public final int getHeight() {
        return height;
    }
    
    /**
     * Returns the width of one tile in this map.
     * @return The width of one tile in this map
     */
    public final int getTileWidth() {
        return tileWidth;
    }
    
    /**
     * Returns the height of one tile in this map.
     * @return The height of one tile in this map
     */
    public final int getTileHeight() {
        return tileHeight;
    }
    
    /**
     * If this map has a hexagonal orientation, returns the width or height
     * (depending on the staggered axis, and in pixels) of each tile's edge.
     * Returns -1 if this map does not have a hexagonal orientation.
     * @return The width or height of each tile's edge, if this map has a
     * hexagonal orientation
     */
    public final int getHexSideLength() {
        return hexSideLength;
    }
    
    /**
     * If this map has a staggered or hexagonal orientation, returns which axis
     * is staggered. Returns null otherwise.
     * @return Which axis is staggered, if this map has a staggered or hexagonal
     * orientation
     */
    public final StaggerAxis getStaggerAxis() {
        return staggerAxis;
    }
    
    /**
     * If this map has a staggered or hexagonal orientation, returns whether the
     * even or odd indices along the staggered axis are shifted. Returns null
     * otherwise.
     * @return Whether the even or odd indices along the staggered axis are
     * shifted, if this map has a staggered or hexagonal orientation
     */
    public final StaggerIndex getStaggerIndex() {
        return staggerIndex;
    }
    
    /**
     * Returns this map's background color, or null if none was specified.
     * @return This map's background color
     */
    public final Color getBackgroundColor() {
        return backgroundColor;
    }
    
    /**
     * Returns an unmodifiable List view of the tilesets included in this map,
     * whether they are embedded directly in the map file or referenced via a
     * link to a TSX file.
     * @return The tilesets included in this map
     */
    public final List<TiledTileset> getTilesets() {
        return tilesets;
    }
    
    /**
     * Returns an unmodifiable List view of all of this map's layers that are
     * not descendants of group layers, sorted from back to front in terms of
     * rendering order. To explore the hierarchy of all of this map's layers,
     * you can iterate through this list and, whenever you encounter a group
     * layer, recursively explore the list of its child layers.
     * @return This map's top-level layers
     */
    public final List<TiledLayer> getTopLevelLayers() {
        return nonGroupLayers;
    }
    
    /**
     * Returns an unmodifiable List view of all of this map's layers that are
     * not group layers, sorted from back to front in terms of rendering order.
     * These layers are the ones that contain actual content, as group layers
     * are just an organizational tool.
     * @return This map's non-group layers
     */
    public final List<TiledLayer> getNonGroupLayers() {
        return nonGroupLayers;
    }
    
    /**
     * Returns an unmodifiable Map view of this map's custom properties. Each
     * key in the Map is the name of a property, and its corresponding value is
     * the value of that property. The type of the value object corresponds
     * to the type of the property: String for a string property, Integer for an
     * int, Float for a float, Boolean for a bool, <code>java.awt.Color</code>
     * for a color, and <code>java.io.File</code> for a file.
     * @return This map's custom properties
     */
    public final Map<String,Object> getProperties() {
        return properties;
    }
    
    /**
     * Returns the value of this map's custom property with the specified name,
     * or null if no such property was specified. The type of the returned value
     * corresponds to the type of the property: String for a string property,
     * Integer for an int, Float for a float, Boolean for a bool, <code>
     * java.awt.Color</code> for a color, and <code>java.io.File</code> for a
     * file.
     * @param name The name of the property whose value is to be returned
     * @return The value of this map's custom property with the specified name
     */
    public final Object getProperty(String name) {
        return properties.get(name);
    }
    
}
