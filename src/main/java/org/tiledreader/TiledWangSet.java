package org.tiledreader;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * <p>A TiledWangSet represents a Wang set defined for a tileset. It corresponds
 * to a &lt;wangset&gt; tag in a Tiled XML file.</p>
 * @author Alex Heyman
 */
public class TiledWangSet implements TiledCustomizable {
    
    private final String name;
    private final TiledTile tile;
    private final List<TiledWangColor> colors;
    private final Map<TiledTile,TiledWangTile> wangTiles;
    private final Map<String,Object> properties;
    
    TiledWangSet(String name, TiledTile tile, List<TiledWangColor> colors,
            Map<TiledTile,TiledWangTile> wangTiles, Map<String,Object> properties) {
        this.name = name;
        this.tile = tile;
        this.colors = (colors == null ? Collections.emptyList() : Collections.unmodifiableList(colors));
        this.wangTiles = (wangTiles == null ?
                Collections.emptyMap() : Collections.unmodifiableMap(wangTiles));
        this.properties = (properties == null ?
                Collections.emptyMap() : Collections.unmodifiableMap(properties));
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
     * Returns an unmodifiable List view of this Wang set's colors.
     * @return This Wang set's colors
     */
    public final List<TiledWangColor> getColors() {
        return colors;
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
    
    @Override
    public final Map<String,Object> getProperties() {
        return properties;
    }
    
    @Override
    public final Object getProperty(String name) {
        return properties.get(name);
    }
    
}
