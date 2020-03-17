package org.tiledreader;

/**
 *
 * @author Alex Heyman
 */
public class TiledWangTile {
    
    private final TiledTile tile;
    private final TiledWangColor[] colors;
    
    TiledWangTile(TiledTile tile, TiledWangColor[] colors) {
        this.tile = tile;
        this.colors = colors;
    }
    
    public final TiledTile getTile() {
        return tile;
    }
    
    public final TiledWangColor getTopColor() {
        return colors[0];
    }
    
    public final TiledWangColor getTopRightColor() {
        return colors[1];
    }
    
    public final TiledWangColor getRightColor() {
        return colors[2];
    }
    
    public final TiledWangColor getBottomRightColor() {
        return colors[3];
    }
    
    public final TiledWangColor getBottomColor() {
        return colors[4];
    }
    
    public final TiledWangColor getBottomLeftColor() {
        return colors[5];
    }
    
    public final TiledWangColor getLeftColor() {
        return colors[6];
    }
    
    public final TiledWangColor getTopLeftColor() {
        return colors[7];
    }
    
}
