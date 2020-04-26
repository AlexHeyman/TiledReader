package org.tiledreader;

import java.awt.Point;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * <p>A TiledTileLayer represents a tile layer. It corresponds to a
 * &lt;layer&gt; tag in a Tiled XML file.</p>
 * @author Alex Heyman
 */
public class TiledTileLayer extends TiledLayer {
    
    static final byte FL_FLIPX = 1;
    static final byte FL_FLIPY = 1 << 1;
    static final byte FL_FLIPD = 1 << 2;
    
    private int x1, y1, x2, y2;
    private final Map<Point,TiledTile> tiles;
    private final Map<Point,Integer> flags;
    
    TiledTileLayer(String name, TiledGroupLayer parent, float relOpacity, boolean relVisible,
            float relOffsetX, float relOffsetY, Map<Point,TiledTile> tiles, Map<Point,Integer> flags) {
        super(name, parent, relOpacity, relVisible, relOffsetX, relOffsetY);
        x1 = 0;
        y1 = 0;
        x2 = -1;
        y2 = -1;
        if (tiles == null) {
            this.tiles = Collections.emptyMap();
        } else {
            this.tiles = Collections.unmodifiableMap(tiles);
            boolean firstTile = true;
            for (Point point : tiles.keySet()) {
                if (firstTile) {
                    firstTile = false;
                    x1 = point.x;
                    y1 = point.y;
                    x2 = point.x;
                    y2 = point.y;
                } else {
                    x1 = Math.min(x1, point.x);
                    y1 = Math.min(y1, point.y);
                    x2 = Math.max(x2, point.x);
                    y2 = Math.max(y2, point.y);
                }
            }
        }
        this.flags = (flags == null ? Collections.emptyMap() : Collections.unmodifiableMap(flags));
    }
    
    /**
     * Returns an unmodifiable Set view of the (x, y) locations in this tile
     * layer's grid where tiles are located.
     * @return The locations in this tile layer's grid where tiles are located
     */
    public final Set<Point> getTileLocations() {
        return tiles.keySet();
    }
    
    /**
     * Returns the lowest x-coordinate in tiles of any tile in this tile layer,
     * or 0 if this tile layer has no tiles.
     * @return The lowest x-coordinate in tiles of any tile in this tile layer
     */
    public final int getX1() {
        return x1;
    }
    
    /**
     * Returns the lowest y-coordinate in tiles of any tile in this tile layer,
     * or 0 if this tile layer has no tiles.
     * @return The lowest y-coordinate in tiles of any tile in this tile layer
     */
    public final int getY1() {
        return y1;
    }
    
    /**
     * Returns the highest x-coordinate in tiles of any tile in this tile layer,
     * or -1 if this tile layer has no tiles.
     * @return The highest x-coordinate in tiles of any tile in this tile layer
     */
    public final int getX2() {
        return x2;
    }
    
    /**
     * Returns the highest y-coordinate in tiles of any tile in this tile layer,
     * or -1 if this tile layer has no tiles.
     * @return The highest y-coordinate in tiles of any tile in this tile layer
     */
    public final int getY2() {
        return y2;
    }
    
    /**
     * Returns the tile at the specified location in this tile layer's grid, or
     * null if there is none.
     * @param x The x-coordinate in tiles of the location
     * @param y The y-coordinate in tiles of the location
     * @return The tile at the specified location
     */
    public final TiledTile getTile(int x, int y) {
        return tiles.get(new Point(x, y));
    }
    
    /**
     * Returns whether the tile at the specified location in this tile layer's
     * grid is flipped horizontally, or false if there is no tile there.
     * @param x The x-coordinate in tiles of the location
     * @param y The y-coordinate in tiles of the location
     * @return whether the tile at the specified location is flipped
     * horizontally
     */
    public final boolean getTileHorizontalFlip(int x, int y) {
        return ((flags.getOrDefault(new Point(x, y), 0) & FL_FLIPX) != 0);
    }
    
    /**
     * Returns whether the tile at the specified location in this tile layer's
     * grid is flipped vertically, or false if there is no tile there.
     * @param x The x-coordinate in tiles of the location
     * @param y The y-coordinate in tiles of the location
     * @return whether the tile at the specified location is flipped vertically
     */
    public final boolean getTileVerticalFlip(int x, int y) {
        return ((flags.getOrDefault(new Point(x, y), 0) & FL_FLIPY) != 0);
    }
    
    /**
     * Returns whether the tile at the specified location in this tile layer's
     * grid is flipped diagonally, or false if there is no tile there.
     * @param x The x-coordinate in tiles of the location
     * @param y The y-coordinate in tiles of the location
     * @return whether the tile at the specified location is flipped diagonally
     */
    public final boolean getTileDiagonalFlip(int x, int y) {
        return ((flags.getOrDefault(new Point(x, y), 0) & FL_FLIPD) != 0);
    }
    
}
