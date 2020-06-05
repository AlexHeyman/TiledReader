package org.tiledreader;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>A TiledGroupLayer represents a group layer. It corresponds to a
 * &lt;group&gt; tag in a Tiled XML file.</p>
 * @author Alex Heyman
 */
public class TiledGroupLayer extends TiledLayer {
    
    private List<TiledLayer> children = new ArrayList<>();
    
    TiledGroupLayer(String name, TiledGroupLayer parent, float relOpacity, boolean relVisible,
            Color relTintColor, float relOffsetX, float relOffsetY) {
        super(name, parent, relOpacity, relVisible, relTintColor, relOffsetX, relOffsetY);
    }
    
    final void addChild(TiledLayer child) {
        children.add(child);
    }
    
    final void finalizeChildren() {
        children = Collections.unmodifiableList(children);
    }
    
    /**
     * Returns an unmodifiable List view of the layers contained in this group
     * layer, sorted from back to front in terms of rendering order.
     * @return The layers in this group layer
     */
    public final List<TiledLayer> getChildren() {
        return children;
    }
    
}
