package org.tiledreader;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Alex Heyman
 */
public class TiledWangSet {
    
    private final String name;
    private final TiledTile tile;
    private final List<TiledWangColor> cornerColors;
    private final List<TiledWangColor> edgeColors;
    private final Map<TiledTile,TiledWangTile> wangTiles;
    
    TiledWangSet(String name, TiledTile tile, List<TiledWangColor> cornerColors,
            List<TiledWangColor> edgeColors, Map<TiledTile,TiledWangTile> wangTiles) {
        this.name = name;
        this.tile = tile;
        this.cornerColors = (cornerColors == null ?
                Collections.emptyList() : Collections.unmodifiableList(cornerColors));
        this.edgeColors = (edgeColors == null ?
                Collections.emptyList() : Collections.unmodifiableList(edgeColors));
        this.wangTiles = (wangTiles == null ?
                Collections.emptyMap() : Collections.unmodifiableMap(wangTiles));
    }
    
    public final String getName() {
        return name;
    }
    
    public final TiledTile getTile() {
        return tile;
    }
    
    public final List<TiledWangColor> getCornerColors() {
        return cornerColors;
    }
    
    public final List<TiledWangColor> getEdgeColors() {
        return edgeColors;
    }
    
    public final Map<TiledTile,TiledWangTile> getWangTiles() {
        return wangTiles;
    }
    
}
