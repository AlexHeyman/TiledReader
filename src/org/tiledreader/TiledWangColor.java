package org.tiledreader;

import java.awt.Color;

/**
 *
 * @author Alex Heyman
 */
public class TiledWangColor {
    
    private final String name;
    private final Color color;
    private final TiledTile tile;
    private final float probability;
    
    TiledWangColor(String name, Color color, TiledTile tile, float probability) {
        this.name = name;
        this.color = color;
        this.tile = tile;
        this.probability = probability;
    }
    
    public final String getName() {
        return name;
    }
    
    public final Color getColor() {
        return color;
    }
    
    public final TiledTile getTile() {
        return tile;
    }
    
    public final float getProbability() {
        return probability;
    }
    
}
