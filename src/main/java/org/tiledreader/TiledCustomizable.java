package org.tiledreader;

import java.util.Map;

/**
 * <p>A TiledCustomizable object represents an entity (such as a map, map layer,
 * or tileset) that can be given custom properties, defined by a
 * &lt;properties&gt; tag inside the entity's tag in a Tiled XML file.</p>
 * @author Alex Heyman
 */
public interface TiledCustomizable {
    
    /**
     * Returns an unmodifiable Map view of this entity's custom properties. Each
     * key in the Map is the name of a property, and its corresponding value is
     * the value of that property. The type of the value object corresponds
     * to the type of the property: String for a string property, Integer for an
     * int, Float for a float, Boolean for a bool, <code>java.awt.Color</code>
     * for a color, TiledFile for a file, and TiledObject for an object (unless
     * the object property is unset, in which case the value is null).
     * @return This entity's custom properties
     */
    Map<String,Object> getProperties();
    
    /**
     * Returns the value of this entity's custom property with the specified
     * name, or null if no such property was specified. The type of the returned
     * value object corresponds to the type of the property: String for a string
     * property, Integer for an int, Float for a float, Boolean for a bool,
     * <code>java.awt.Color</code> for a color, TiledFile for a file, and
     * TiledObject for an object (unless the object property is unset, in which
     * case the value is null).
     * @param name The name of the property whose value is to be returned
     * @return The value of this entity's custom property with the specified
     * name
     */
    Object getProperty(String name);
    
}
