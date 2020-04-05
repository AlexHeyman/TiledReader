package org.tiledreader;

/**
 * <p>A TiledWangTile represents a Wang tile. It corresponds to a
 * &lt;wangtile&gt; tag in a Tiled XML file.</p>
 * @author Alex Heyman
 */
public class TiledWangTile {
    
    private final TiledTile tile;
    private final TiledWangColor[] colors;
    
    TiledWangTile(TiledTile tile, TiledWangColor[] colors) {
        this.tile = tile;
        this.colors = colors;
    }
    
    /**
     * Returns the tile from this Wang tile's Wang set's tileset to which this
     * Wang tile refers.
     * @return The tile to which this Wang tile refers
     */
    public final TiledTile getTile() {
        return tile;
    }
    
    /**
     * Returns this Wang tile's color on its top edge, or null if there is none.
     * @return This Wang tile's color on its top edge
     */
    public final TiledWangColor getTopColor() {
        return colors[0];
    }
    
    /**
     * Returns this Wang tile's color at its top right corner, or null if there
     * is none.
     * @return This Wang tile's color at its top right corner
     */
    public final TiledWangColor getTopRightColor() {
        return colors[1];
    }
    
    /**
     * Returns this Wang tile's color on its right edge, or null if there is
     * none.
     * @return This Wang tile's color on its right edge
     */
    public final TiledWangColor getRightColor() {
        return colors[2];
    }
    
    /**
     * Returns this Wang tile's color at its bottom right corner, or null if
     * there is none.
     * @return This Wang tile's color at its bottom right corner
     */
    public final TiledWangColor getBottomRightColor() {
        return colors[3];
    }
    
    /**
     * Returns this Wang tile's color on its bottom edge, or null if there is
     * none.
     * @return This Wang tile's color on its right edge
     */
    public final TiledWangColor getBottomColor() {
        return colors[4];
    }
    
    /**
     * Returns this Wang tile's color at its bottom left corner, or null if
     * there is none.
     * @return This Wang tile's color at its bottom left corner
     */
    public final TiledWangColor getBottomLeftColor() {
        return colors[5];
    }
    
    /**
     * Returns this Wang tile's color on its left edge, or null if there is
     * none.
     * @return This Wang tile's color on its left edge
     */
    public final TiledWangColor getLeftColor() {
        return colors[6];
    }
    
    /**
     * Returns this Wang tile's color at its top left corner, or null if there
     * is none.
     * @return This Wang tile's color at its top left corner
     */
    public final TiledWangColor getTopLeftColor() {
        return colors[7];
    }
    
}
