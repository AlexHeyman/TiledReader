package org.tiledreader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Alex Heyman
 */
public class TiledGroupLayer extends TiledLayer {
    
    private List<TiledLayer> children = new ArrayList<>();
    
    TiledGroupLayer(String name, TiledGroupLayer parent, float relOpacity, boolean relVisible,
            float relOffsetX, float relOffsetY) {
        super(name, parent, relOpacity, relVisible, relOffsetX, relOffsetY);
    }
    
    final void addChild(TiledLayer child) {
        children.add(child);
    }
    
    final void finalizeChildren() {
        children = Collections.unmodifiableList(children);
    }
    
    /**
     * Returns an unmodifiable List view of the layers in this group layer,
     * ordered from back to front.
     * @return The layers in this group layer
     */
    public final List<TiledLayer> getChildren() {
        return children;
    }
    
}
