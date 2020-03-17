package org.tiledreader;

/**
 *
 * @author Alex Heyman
 */
public class TiledImageLayer extends TiledLayer {
    
    private final TiledImage image;
    
    TiledImageLayer(String name, TiledGroupLayer parent, float relOpacity, boolean relVisible,
            float relOffsetX, float relOffsetY, TiledImage image) {
        super(name, parent, relOpacity, relVisible, relOffsetX, relOffsetY);
        this.image = image;
    }
    
    public final TiledImage getImage() {
        return image;
    }
    
}
