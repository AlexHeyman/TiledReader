package org.tiledreader;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * <p>A TiledWangSet represents a Wang set defined for a tileset. It corresponds
 * to a &lt;wangset&gt; tag in a Tiled XML file.</p>
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
    
    /**
     * Returns this Wang set's name.
     * @return This Wang set's name
     */
    public final String getName() {
        return name;
    }
    
    /**
     * Returns the tile from this Wang set's tileset that represents this Wang
     * set, or null if none was specified.
     * @return The tile that represents this Wang set
     */
    public final TiledTile getTile() {
        return tile;
    }
    
    /**
     * Returns an unmodifiable List view of this Wang set's corner colors.
     * @return This Wang set's corner colors
     */
    public final List<TiledWangColor> getCornerColors() {
        return cornerColors;
    }
    
    /**
     * Returns an unmodifiable List view of this Wang set's edge colors.
     * @return This Wang set's edge colors
     */
    public final List<TiledWangColor> getEdgeColors() {
        return edgeColors;
    }
    
    /**
     * Returns an unmodifiable Map view of this Wang set's Wang tiles. Each key
     * in the Map is a tile from this Wang set's tileset, and its corresponding
     * value is the Wang tile that refers to that tile.
     * @return This Wang set's Wang tiles
     */
    public final Map<TiledTile,TiledWangTile> getWangTiles() {
        return wangTiles;
    }
    
}
