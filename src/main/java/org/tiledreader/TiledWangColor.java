package org.tiledreader;

import java.awt.*;

/**
 * <p>A TiledWangColor represents a color that can be used to define the corner
 * or edge of a Wang tile. It corresponds to a &lt;wangcornercolor&gt; or
 * &lt;wangedgecolor&gt; tag in a Tiled XML file.</p>
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
    
    /**
     * Returns this color's name.
     * @return This color's name
     */
    public final String getName() {
        return name;
    }
    
    /**
     * Returns this color's value; that is, the actual color.
     * @return This color's value
     */
    public final Color getColor() {
        return color;
    }
    
    /**
     * Returns the tile from this color's tileset that represents this color, or
     * null if none was specified.
     * @return The tile that represents this color
     */
    public final TiledTile getTile() {
        return tile;
    }
    
    /**
     * Returns the relative probability that this color is chosen over others in
     * case of multiple options.
     * @return The probability that this color is chosen in case of multiple
     * options
     */
    public final float getProbability() {
        return probability;
    }
    
}
