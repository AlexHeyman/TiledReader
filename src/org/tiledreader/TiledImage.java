package org.tiledreader;

import java.awt.Color;

/**
 * <p>Note that Cell2D does not support image data embedded directly in TMX/TSX
 * files. As of Tiled version 1.3.2, however, it is not possible to embed image
 * data in files using the Tiled editor.</p>
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
    
    public final String getSource() {
        return source;
    }
    
    public final Color getTransColor() {
        return transColor;
    }
    
    /**
     * Returns this image's width, or 0 if it was not specified.
     * @return This image's width
     */
    public final int getWidth() {
        return width;
    }
    
    /**
     * Returns this image's height, or 0 if it was not specified.
     * @return This image's height
     */
    public final int getHeight() {
        return height;
    }
    
}
