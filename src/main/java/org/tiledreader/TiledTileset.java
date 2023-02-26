package org.tiledreader;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * <p>A TiledTileset represents a tileset. It corresponds to a &lt;tileset&gt;
 * tag in a Tiled XML file.</p>
 * @author Alex Heyman
 */
public class TiledTileset extends TiledResource implements TiledCustomizable {
    
    /**
     * <p>Represents an alignment that tile objects using a tileset's tiles can
     * have.</p>
     */
    public static enum ObjectAlignment {
        UNSPECIFIED, TOPLEFT, TOP, TOPRIGHT, LEFT, CENTER, RIGHT, BOTTOMLEFT, BOTTOM, BOTTOMRIGHT;
    }
    
    /**
     * <p>Represents an orientation that a tileset's tile grid can have.</p>
     */
    public static enum GridOrientation {
        ORTHOGONAL, ISOMETRIC
    }
    
    private final String name;
    private final int tileWidth, tileHeight, spacing, margin;
    private final List<TiledTile> tilesInDisplayOrder;
    private final Map<Integer,TiledTile> idTiles;
    private final int width, height;
    private final int tileOffsetX, tileOffsetY;
    private final ObjectAlignment objectAlignment;
    private final GridOrientation gridOrientation;
    private final int gridWidth, gridHeight;
    private final TiledImage image;
    private final List<TiledWangSet> wangSets;
    private final boolean hFlipAllowed, vFlipAllowed, rotateAllowed, preferUntransformed;
    private final Map<String,Object> properties;
    
    TiledTileset(TiledReader reader, String path, String name, int tileWidth, int tileHeight, int spacing,
            int margin, List<TiledTile> tilesInDisplayOrder, Map<Integer,TiledTile> idTiles, int columns,
            int tileOffsetX, int tileOffsetY, ObjectAlignment objectAlignment,
            GridOrientation gridOrientation, int gridWidth, int gridHeight, TiledImage image,
            List<TiledWangSet> wangSets, boolean[] transformations, Map<String,Object> properties) {
        super(reader, path);
        this.name = name;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.spacing = spacing;
        this.margin = margin;
        this.tilesInDisplayOrder = Collections.unmodifiableList(tilesInDisplayOrder);
        this.idTiles = Collections.unmodifiableMap(idTiles);
        width = columns;
        height = (int)Math.ceil(((double)tilesInDisplayOrder.size()) / columns);
        this.tileOffsetX = tileOffsetX;
        this.tileOffsetY = tileOffsetY;
        this.objectAlignment = objectAlignment;
        this.gridOrientation = gridOrientation;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.image = image;
        this.wangSets = (wangSets == null ?
                Collections.emptyList() : Collections.unmodifiableList(wangSets));
        if (transformations == null) {
            hFlipAllowed = false;
            vFlipAllowed = false;
            rotateAllowed = false;
            preferUntransformed = false;
        } else {
            hFlipAllowed = transformations[0];
            vFlipAllowed = transformations[1];
            rotateAllowed = transformations[2];
            preferUntransformed = transformations[3];
        }
        this.properties = (properties == null ?
                Collections.emptyMap() : Collections.unmodifiableMap(properties));
    }
    
    /**
     * Returns this tileset's name.
     * @return This tileset's name
     */
    public final String getName() {
        return name;
    }
    
    /**
     * Returns the width in pixels of each of this tileset's tiles.
     * @return The width in pixels of each of this tileset's tiles
     */
    public final int getTileWidth() {
        return tileWidth;
    }
    
    /**
     * Returns the height in pixels of each of this tileset's tiles.
     * @return The height in pixels of each of this tileset's tiles
     */
    public final int getTileHeight() {
        return tileHeight;
    }
    
    /**
     * Returns the spacing in pixels between this tileset's tiles in the
     * tileset's image (0 by default). If this tileset is an image collection
     * tileset, this value is meaningless.
     * @return The spacing in pixels between this tileset's tiles
     */
    public final int getSpacing() {
        return spacing;
    }
    
    /**
     * Returns the margin in pixels around this tileset's tiles in the tileset's
     * image (0 by default). If this tileset is an image collection tileset,
     * this value is meaningless.
     * @return The margin in pixels around this tileset's tiles
     */
    public final int getMargin() {
        return margin;
    }
    
    /**
     * Returns an unmodifiable List view of this tileset's tiles. The tiles are
     * listed in rows from left to right and top to bottom as seen in the Tiled
     * editor.
     * @return This tileset's tiles
     */
    public final List<TiledTile> getTiles() {
        return tilesInDisplayOrder;
    }
    
    /**
     * Returns the tile in this tileset with the specified local ID, or null if
     * there is none.
     * @param id The local ID of the tile to return
     * @return The tile with the specified local ID
     */
    public final TiledTile getTile(int id) {
        return idTiles.get(id);
    }
    
    /**
     * Returns this tileset's width in tiles, or equivalently its number of
     * columns. This is the width seen in the Tiled editor when "Dynamically
     * Wrap Tiles" is off.
     * @return This tileset's width in tiles
     */
    public final int getWidth() {
        return width;
    }
    
    /**
     * Returns this tileset's height in tiles. This is the height seen in the
     * Tiled editor when "Dynamically Wrap Tiles" is off.
     * @return This tileset's height in tiles
     */
    public final int getHeight() {
        return height;
    }
    
    /**
     * Returns the tile at the specified location in this tileset, or null if
     * there is none. These are the locations seen in the Tiled editor when
     * "Dynamically Wrap Tiles" is off. Note that if an image collection
     * tileset's number of tiles is not evenly divisible by its number of
     * columns, its bottommost row will not extend as far right as the others.
     * @param x The x-coordinate in tiles of the location. x-coordinates range
     * from 0 to getWidth() - 1, increasing from left to right.
     * @param y The y-coordinate in tiles of the location. y-coordinates range
     * from 0 to getHeight() - 1, increasing from top to bottom.
     * @return The tile at the specified location
     */
    public final TiledTile getTile(int x, int y) {
        int displayOrderIndex = y * width + x;
        if (displayOrderIndex >= 0 && displayOrderIndex < tilesInDisplayOrder.size()) {
            return tilesInDisplayOrder.get(displayOrderIndex);
        }
        return null;
    }
    
    /**
     * Returns the horizontal rendering offset in pixels of this tileset's tiles
     * (0 by default).
     * @return The horizontal rendering offset in pixels of this tileset's tiles
     */
    public final int getTileOffsetX() {
        return tileOffsetX;
    }
    
    /**
     * Returns the vertical rendering offset in pixels of this tileset's tiles
     * (0 by default).
     * @return The vertical rendering offset in pixels of this tileset's tiles
     */
    public final int getTileOffsetY() {
        return tileOffsetY;
    }
    
    /**
     * Returns the alignment type of tile objects using this tileset's tiles
     * (<code>TiledTileset.ObjectAlignment.UNSPECIFIED</code> by default).
     * @return The orientation of this tileset's tile grid
     */
    public final ObjectAlignment getObjectAlignment() {
        return objectAlignment;
    }
    
    /**
     * Returns the orientation of this tileset's tile grid
     * (<code>TiledTileset.GridOrientation.ORTHOGONAL</code> by default).
     * @return The orientation of this tileset's tile grid
     */
    public final GridOrientation getGridOrientation() {
        return gridOrientation;
    }
    
    /**
     * Returns the width in pixels of the cells in this tileset's tile grid (by
     * default, equal to getTileWidth()).
     * @return The width in pixels of the cells in this tileset's tile grid
     */
    public final int getGridWidth() {
        return gridWidth;
    }
    
    /**
     * Returns the height in pixels of the cells in this tileset's tile grid (by
     * default, equal to getTileHeight()).
     * @return The height in pixels of the cells in this tileset's tile grid
     */
    public final int getGridHeight() {
        return gridHeight;
    }
    
    /**
     * Returns this tileset's image if it is a single-image tileset, or null if
     * it is an image collection tileset.
     * @return This tileset's image
     */
    public final TiledImage getImage() {
        return image;
    }
    
    /**
     * Returns an unmodifiable List view of this tileset's Wang sets.
     * @return This tileset's Wang sets
     */
    public final List<TiledWangSet> getWangSets() {
        return wangSets;
    }
    
    /**
     * Returns whether this tileset's tiles can be flipped horizontally.
     * @return Whether this tileset's tiles can be flipped horizontally
     */
    public final boolean getHFlipAllowed() {
        return hFlipAllowed;
    }
    
    /**
     * Returns whether this tileset's tiles can be flipped vertically.
     * @return Whether this tileset's tiles can be flipped vertically
     */
    public final boolean getVFlipAllowed() {
        return vFlipAllowed;
    }
    
    /**
     * Returns whether this tileset's tiles can be rotated in 90-degree
     * increments.
     * @return Whether this tileset's tiles can be rotated in 90-degree
     * increments
     */
    public final boolean getRotateAllowed() {
        return rotateAllowed;
    }
    
    /**
     * Returns whether untransformed tiles are preferred from this tileset.
     * @return Whether untransformed tiles are preferred from this tileset
     */
    public final boolean getPreferUntransformed() {
        return preferUntransformed;
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
