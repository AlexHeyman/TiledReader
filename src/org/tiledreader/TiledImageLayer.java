package org.tiledreader;

/**
 * <p>A TiledImageLayer represents an image layer. It corresponds to an
 * &lt;imagelayer&gt; tag in a Tiled XML file.</p>
 * @author Alex Heyman
 */
public class TiledImageLayer extends TiledLayer {
    
    private final TiledImage image;
    
    TiledImageLayer(String name, TiledGroupLayer parent, float relOpacity, boolean relVisible,
            float relOffsetX, float relOffsetY, TiledImage image) {
        super(name, parent, relOpacity, relVisible, relOffsetX, relOffsetY);
        this.image = image;
    }
    
    /**
     * Returns this image layer's image.
     * @return This image layer's image
     */
    public final TiledImage getImage() {
        return image;
    }
    
}
