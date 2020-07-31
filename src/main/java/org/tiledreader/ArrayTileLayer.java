package org.tiledreader;

import java.awt.Color;
import java.awt.Point;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * <p>An ArrayTileLayer is a type of TiledTileLayer that stores its tile data in
 * a rectangular two-dimensional array. An ArrayTileLayer's memory usage, and
 * the time needed to iterate through the set of its tile locations, are both
 * proportional to the area in square tiles of the smallest rectangle that fits
 * around all of the tiles in the tile layer it represents. The TiledReader
 * class automatically chooses whether to represent each tile layer it reads
 * with an ArrayTileLayer or a HashTileLayer, based on an estimate of which
 * would use less memory.</p>
 * @author Alex Heyman
 */
public class ArrayTileLayer extends TiledTileLayer {
    
    private final int x1, y1;
    private final TiledTile[][] tiles;
    private final int numNonNullTiles;
    private final byte[][] flags;
    
    ArrayTileLayer(String name, TiledGroupLayer parent, float relOpacity, boolean relVisible,
            Color relTintColor, float relOffsetX, float relOffsetY, int x1, int y1, int x2, int y2,
            Map<Point,TiledTile> tiles, Map<Point,Integer> flags) {
        super(name, parent, relOpacity, relVisible, relTintColor, relOffsetX, relOffsetY);
        this.x1 = x1;
        this.y1 = y1;
        int numColumns = x2 - x1 + 1;
        int numRows = y2 - y1 + 1;
        this.tiles = new TiledTile[numColumns][numRows];
        this.flags = new byte[numColumns][numRows];
        if (tiles == null) {
            numNonNullTiles = 0;
        } else {
            numNonNullTiles = tiles.size();
            for (Map.Entry<Point,TiledTile> entry : tiles.entrySet()) {
                Point point = entry.getKey();
                this.tiles[point.x - x1][point.y - y1] = entry.getValue();
            }
            for (Map.Entry<Point,Integer> entry : flags.entrySet()) {
                Point point = entry.getKey();
                this.flags[point.x - x1][point.y - y1] = (byte)(int)entry.getValue();
            }
        }
    }
    
    private class TileLocationsIterator implements Iterator<Point> {
        
        private int i, j;
        
        private TileLocationsIterator() {
            i = -1;
            j = 0;
            advance();
        }
        
        private void advance() {
            do {
                i++;
                if (i == tiles.length) {
                    i = 0;
                    j++;
                }
            } while (j < tiles[0].length && tiles[i][j] == null);
        }
        
        @Override
        public final boolean hasNext() {
            return j < tiles[0].length;
        }
        
        @Override
        public final Point next() {
            Point next = new Point(x1 + i, y1 + j);
            advance();
            return next;
        }
        
    }
    
    private class TileLocationsSet extends AbstractSet<Point> {
        
        @Override
        public final int size() {
            return numNonNullTiles;
        }
        
        @Override
        public final boolean contains(Object o) {
            if (o instanceof Point) {
                Point point = (Point)o;
                int i = point.x - x1;
                int j = point.y - y1;
                if (i >= 0 && i < tiles.length && j >= 0 && j < tiles[0].length) {
                    return (tiles[i][j] != null);
                }
            }
            return false;
        }
        
        @Override
        public final Iterator<Point> iterator() {
            return new TileLocationsIterator();
        }
        
    }
    
    @Override
    public final Set<Point> getTileLocations() {
        return new TileLocationsSet();
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
        return x1 + tiles.length - 1;
    }
    
    @Override
    public final int getY2() {
        return y1 + tiles[0].length - 1;
    }
    
    @Override
    public final TiledTile getTile(int x, int y) {
        int i = x - x1;
        int j = y - y1;
        if (i < 0 || i >= tiles.length || j < 0 || j >= tiles[0].length) {
            return null;
        }
        return tiles[i][j];
    }
    
    @Override
    public final boolean getTileHorizontalFlip(int x, int y) {
        int i = x - x1;
        int j = y - y1;
        if (i < 0 || i >= tiles.length || j < 0 || j >= tiles[0].length) {
            return false;
        }
        return (flags[i][j] & FL_FLIPX) != 0;
    }
    
    @Override
    public final boolean getTileVerticalFlip(int x, int y) {
        int i = x - x1;
        int j = y - y1;
        if (i < 0 || i >= tiles.length || j < 0 || j >= tiles[0].length) {
            return false;
        }
        return (flags[i][j] & FL_FLIPY) != 0;
    }
    
    @Override
    public final boolean getTileDiagonalFlip(int x, int y) {
        int i = x - x1;
        int j = y - y1;
        if (i < 0 || i >= tiles.length || j < 0 || j >= tiles[0].length) {
            return false;
        }
        return (flags[i][j] & FL_FLIPD) != 0;
    }
    
}
