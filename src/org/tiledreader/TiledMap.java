package org.tiledreader;

import java.awt.Color;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Alex Heyman
 */
public class TiledMap {
    
    public static enum Orientation {
        ORTHOGONAL, ISOMETRIC, STAGGERED, HEXAGONAL
    }
    
    public static enum RenderOrder {
        RIGHT_DOWN, RIGHT_UP, LEFT_DOWN, LEFT_UP
    }
    
    public static enum StaggerAxis {
        X, Y
    }
    
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
    private final List<TiledLayer> layers;
    private final Map<String,Object> properties;
    
    TiledMap(Orientation orientation, RenderOrder renderOrder,
            int width, int height, int tileWidth, int tileHeight, int hexSideLength,
            StaggerAxis staggerAxis, StaggerIndex staggerIndex, Color backgroundColor,
            List<TiledTileset> tilesets, List<TiledLayer> layers, Map<String,Object> properties) {
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
        this.layers = Collections.unmodifiableList(layers);
        this.properties = (properties == null ?
                Collections.emptyMap() : Collections.unmodifiableMap(properties));
    }
    
    public final Orientation getOrientation() {
        return orientation;
    }
    
    public final RenderOrder getRenderOrder() {
        return renderOrder;
    }
    
    public final int getWidth() {
        return width;
    }
    
    public final int getHeight() {
        return height;
    }
    
    public final int getTileWidth() {
        return tileWidth;
    }
    
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
     * Returns an unmodifiable List view of this map's layers, in order from
     * back to front in terms of rendering order. The returned List includes
     * both group layers and the layers that are children of group layers, with
     * group layers appearing in the List before their children. To get only the
     * "top-level" layers that are not children of group layers, iterate through
     * the List and ignore any layer whose getParent() method does not return
     * null.
     * @return This map's layers
     */
    public final List<TiledLayer> getLayers() {
        return layers;
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
