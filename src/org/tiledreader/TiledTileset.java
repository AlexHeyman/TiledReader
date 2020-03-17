package org.tiledreader;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

/**
 *
 * @author Alex Heyman
 */
public class TiledTileset {
    
    public static enum GridOrientation {
        ORTHOGONAL, ISOMETRIC
    }
    
    private final String name;
    private final int tileWidth, tileHeight, spacing, margin;
    private final SortedMap<Integer,TiledTile> idTiles;
    private final TiledTile[][] locationTiles;
    private final int width, height;
    private final int tileOffsetX, tileOffsetY;
    private final GridOrientation gridOrientation;
    private final int gridWidth, gridHeight;
    private final TiledImage image;
    private final Map<String,TiledTerrainType> terrainTypes;
    private final List<TiledWangSet> wangSets;
    private final Map<String,Object> properties;
    
    TiledTileset(String name, int tileWidth, int tileHeight, int spacing, int margin,
            SortedMap<Integer,TiledTile> idTiles, int columns, int tileOffsetX, int tileOffsetY,
            GridOrientation gridOrientation, int gridWidth, int gridHeight, TiledImage image,
            Map<String,TiledTerrainType> terrainTypes, List<TiledWangSet> wangSets,
            Map<String,Object> properties) {
        this.name = name;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.spacing = spacing;
        this.margin = margin;
        this.idTiles = Collections.unmodifiableSortedMap(idTiles);
        if (image == null) {
            //Image collection tileset
            width = -1;
            height = -1;
            locationTiles = null;
        } else {
            //Single-image tileset
            width = columns;
            height = idTiles.size() / columns;
            locationTiles = new TiledTile[width][height];
            int x = 0;
            int y = 0;
            for (TiledTile tile : idTiles.values()) {
                tile.tilesetX = x;
                tile.tilesetY = y;
                locationTiles[x][y] = tile;
                x++;
                if (x == width) {
                    x = 0;
                    y++;
                }
            }
        }
        this.tileOffsetX = tileOffsetX;
        this.tileOffsetY = tileOffsetY;
        this.gridOrientation = gridOrientation;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.image = image;
        this.terrainTypes = (terrainTypes == null ?
                Collections.emptyMap() : Collections.unmodifiableMap(terrainTypes));
        this.wangSets = (wangSets == null ?
                Collections.emptyList() : Collections.unmodifiableList(wangSets));
        this.properties = (properties == null ?
                Collections.emptyMap() : Collections.unmodifiableMap(properties));
    }
    
    public final String getName() {
        return name;
    }
    
    public final int getTileWidth() {
        return tileWidth;
    }
    
    public final int getTileHeight() {
        return tileHeight;
    }

    public final int getSpacing() {
        return spacing;
    }
    
    public final int getMargin() {
        return margin;
    }
    
    /**
     * Returns an unmodifiable Collection view of this tileset's tiles. The
     * Collection's iterator will return the tiles in order from lowest to
     * highest local ID.
     * @return This tileset's tiles
     */
    public final Collection<TiledTile> getTiles() {
        return idTiles.values();
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
     * Returns this tileset's width in tiles, or -1 if this tileset is an image
     * collection tileset.
     * @return This tileset's width in tiles
     */
    public final int getWidth() {
        return width;
    }
    
    /**
     * Returns this tileset's height in tiles, or -1 if this tileset is an image
     * collection tileset.
     * @return This tileset's height in tiles
     */
    public final int getHeight() {
        return height;
    }
    
    /**
     * Returns the tile at the specified location in this tileset, if this
     * tileset is a single-image tileset.
     * @param x The x-coordinate in tiles of the location
     * @param y The y-coordinate in tiles of the location
     * @return The tile at the specified location
     * @throws UnsupportedOperationException if this tileset is an image
     * collection tileset
     * @throws IndexOutOfBoundsException if the location is out of this
     * tileset's bounds
     */
    public final TiledTile getTile(int x, int y) {
        if (locationTiles == null) {
            throw new UnsupportedOperationException("Attempted to retrieve a TiledTile at a specific"
                    + " location from an image collection TiledTileset");
        }
        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new IndexOutOfBoundsException("Attempted to retrieve a TiledTile from a TiledTileset at"
                    + " invalid coordinates (" + x + ", " + y + ")");
        }
        return locationTiles[x][y];
    }
    
    public final int getTileOffsetX() {
        return tileOffsetX;
    }
    
    public final int getTileOffsetY() {
        return tileOffsetY;
    }
    
    public final GridOrientation getGridOrientation() {
        return gridOrientation;
    }
    
    public final int getGridWidth() {
        return gridWidth;
    }
    
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
    
    public final Map<String,TiledTerrainType> getTerrainTypes() {
        return terrainTypes;
    }
    
    public final List<TiledWangSet> getWangSets() {
        return wangSets;
    }
    
    public final Map<String,Object> getProperties() {
        return properties;
    }
    
    public final Object getProperty(String name) {
        return properties.get(name);
    }
    
}
