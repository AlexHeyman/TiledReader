package org.tiledreader;

import java.awt.Color;
import java.awt.Point;
import java.util.Set;

/**
 * <p>A TiledTileLayer represents a tile layer. It corresponds to a
 * &lt;layer&gt; tag in a Tiled XML file.</p>
 * @author Alex Heyman
 */
public abstract class TiledTileLayer extends TiledLayer {
    
    static final byte FL_FLIPX = 1;
    static final byte FL_FLIPY = 1 << 1;
    static final byte FL_FLIPD = 1 << 2;
    
    TiledTileLayer(String name, TiledGroupLayer parent, float relOpacity, boolean relVisible,
            Color relTintColor, float relOffsetX, float relOffsetY) {
        super(name, parent, relOpacity, relVisible, relTintColor, relOffsetX, relOffsetY);
    }
    
    /**
     * Returns an unmodifiable Set view of the (x, y) locations in this tile
     * layer's grid where tiles are located.
     * @return The locations in this tile layer's grid where tiles are located
     */
    public abstract Set<Point> getTileLocations();
    
    /**
     * Returns the lowest x-coordinate in tiles of any tile in this tile layer,
     * or 0 if this tile layer has no tiles.
     * @return The lowest x-coordinate in tiles of any tile in this tile layer
     */
    public abstract int getX1();
    
    /**
     * Returns the lowest y-coordinate in tiles of any tile in this tile layer,
     * or 0 if this tile layer has no tiles.
     * @return The lowest y-coordinate in tiles of any tile in this tile layer
     */
    public abstract int getY1();
    
    /**
     * Returns the highest x-coordinate in tiles of any tile in this tile layer,
     * or -1 if this tile layer has no tiles.
     * @return The highest x-coordinate in tiles of any tile in this tile layer
     */
    public abstract int getX2();
    
    /**
     * Returns the highest y-coordinate in tiles of any tile in this tile layer,
     * or -1 if this tile layer has no tiles.
     * @return The highest y-coordinate in tiles of any tile in this tile layer
     */
    public abstract int getY2();
    
    /**
     * Returns the tile at the specified location in this tile layer's grid, or
     * null if there is none.
     * @param x The x-coordinate in tiles of the location
     * @param y The y-coordinate in tiles of the location
     * @return The tile at the specified location
     */
    public abstract TiledTile getTile(int x, int y);
    
    /**
     * Returns whether the tile at the specified location in this tile layer's
     * grid is flipped horizontally, or false if there is no tile there.
     * @param x The x-coordinate in tiles of the location
     * @param y The y-coordinate in tiles of the location
     * @return whether the tile at the specified location is flipped
     * horizontally
     */
    public abstract boolean getTileHorizontalFlip(int x, int y);
    
    /**
     * Returns whether the tile at the specified location in this tile layer's
     * grid is flipped vertically, or false if there is no tile there.
     * @param x The x-coordinate in tiles of the location
     * @param y The y-coordinate in tiles of the location
     * @return whether the tile at the specified location is flipped vertically
     */
    public abstract boolean getTileVerticalFlip(int x, int y);
    
    /**
     * Returns whether the tile at the specified location in this tile layer's
     * grid is flipped diagonally, or false if there is no tile there.
     * @param x The x-coordinate in tiles of the location
     * @param y The y-coordinate in tiles of the location
     * @return whether the tile at the specified location is flipped diagonally
     */
    public abstract boolean getTileDiagonalFlip(int x, int y);
    
}
