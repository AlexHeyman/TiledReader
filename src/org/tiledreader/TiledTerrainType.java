package org.tiledreader;

/**
 * <p>A TiledTerrainType represents a terrain type in a tileset. It corresponds
 * to a &lt;terrain&gt; tag in a Tiled XML file.</p>
 * @author Alex Heyman
 */
public class TiledTerrainType {
    
    private final String name;
    private final TiledTile tile;
    
    TiledTerrainType(String name, TiledTile tile) {
        this.name = name;
        this.tile = tile;
    }
    
    /**
     * Returns this terrain type's name.
     * @return This terrain type's name
     */
    public final String getName() {
        return name;
    }
    
    /**
     * Returns the tile in this terrain type's tileset that represents this
     * terrain type visually, or null if there is no such tile.
     * @return The tile that represents this terrain type visually
     */
    public final TiledTile getTile() {
        return tile;
    }
    
}
