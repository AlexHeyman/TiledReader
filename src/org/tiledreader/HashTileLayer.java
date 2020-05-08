package org.tiledreader;

import java.awt.Point;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * <p>A HashTileLayer is a type of TiledTileLayer that stores its tile data in
 * HashMaps with tile grid locations as keys. A HashTileLayer's memory usage is
 * proportional to the number of occupied grid locations in the tile layer it
 * represents, plus the number of tiles in the layer that are horizontally,
 * vertically, or diagonally flipped. The time needed to iterate through a
 * HashTileLayer's set of tile locations is proportional only to the number of
 * those locations. The TiledReader class automatically chooses whether to
 * represent each tile layer it reads with an ArrayTileLayer or a HashTileLayer,
 * based on an estimate of which would use less memory.</p>
 * @author Alex Heyman
 */
public class HashTileLayer extends TiledTileLayer {
    
    private final int x1, y1, x2, y2;
    private final Map<Point,TiledTile> tiles;
    private final Map<Point,Integer> flags;
    
    HashTileLayer(String name, TiledGroupLayer parent, float relOpacity, boolean relVisible,
            float relOffsetX, float relOffsetY, int x1, int y1, int x2, int y2,
            Map<Point,TiledTile> tiles, Map<Point,Integer> flags) {
        super(name, parent, relOpacity, relVisible, relOffsetX, relOffsetY);
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        if (tiles == null) {
            this.tiles = Collections.emptyMap();
            this.flags = Collections.emptyMap();
        } else {
            this.tiles = Collections.unmodifiableMap(tiles);
            this.flags = Collections.unmodifiableMap(flags);
        }
    }
    
    @Override
    public final Set<Point> getTileLocations() {
        return tiles.keySet();
    }
    
    @Override
    public final int getX1() {
        return x1;
    }
    
    @Override
    public final int getY1() {
        return y1;
    }
    
    @Override
    public final int getX2() {
        return x2;
    }
    
    @Override
    public final int getY2() {
        return y2;
    }
    
    @Override
    public final TiledTile getTile(int x, int y) {
        return tiles.get(new Point(x, y));
    }
    
    @Override
    public final boolean getTileHorizontalFlip(int x, int y) {
        return ((flags.getOrDefault(new Point(x, y), 0) & FL_FLIPX) != 0);
    }
    
    @Override
    public final boolean getTileVerticalFlip(int x, int y) {
        return ((flags.getOrDefault(new Point(x, y), 0) & FL_FLIPY) != 0);
    }
    
    @Override
    public final boolean getTileDiagonalFlip(int x, int y) {
        return ((flags.getOrDefault(new Point(x, y), 0) & FL_FLIPD) != 0);
    }
    
}
