package org.tiledreader;

import java.awt.Color;

/**
 * <p>A TiledImage represents the image associated with a single-image tileset,
 * a tile in an image collection tileset, or an image layer. It corresponds to
 * an &lt;image&gt; tag in a Tiled XML file.</p>
 * @author Alex Heyman
 */
public class TiledImage {
    
    private final String source;
    private final Color transColor;
    private final int width;
    private final int height;
    
    TiledImage(String source, Color transColor, int width, int height) {
        this.source = source;
        this.transColor = transColor;
        this.width = width;
        this.height = height;
    }
    
    /**
     * Returns the relative path (from the program's working directory) to this
     * image's file.
     * @return The relative path to this image's file
     */
    public final String getSource() {
        return source;
    }
    
    /**
     * Returns the color that is treated as transparent in this image, or null
     * if none was specified.
     * @return This image's transparent color
     */
    public final Color getTransColor() {
        return transColor;
    }
    
    /**
     * Returns this image's width, or -1 if it was not specified.
     * @return This image's width
     */
    public final int getWidth() {
        return width;
    }
    
    /**
     * Returns this image's height, or -1 if it was not specified.
     * @return This image's height
     */
    public final int getHeight() {
        return height;
    }
    
}
