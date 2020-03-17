package org.tiledreader;

/**
 *
 * @author Alex Heyman
 */
public class TiledTerrainType {
    
    private final String name;
    private final TiledTile tile;
    
    TiledTerrainType(String name, TiledTile tile) {
        this.name = name;
        this.tile = tile;
    }
    
    public final String getName() {
        return name;
    }
    
    public final TiledTile getTile() {
        return tile;
    }
    
}
