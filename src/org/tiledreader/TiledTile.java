package org.tiledreader;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Alex Heyman
 */
public class TiledTile {
    
    TiledTileset tileset = null;
    private final int id;
    int tilesetX = -1;
    int tilesetY = -1;
    private TiledImage image = null;
    String type = null;
    final TiledTerrainType[] terrainTypes = new TiledTerrainType[4];
    float probability = -1;
    private List<TiledObject> collisionObjects = Collections.emptyList();
    private List<TiledTile> frames = Collections.emptyList();
    private List<Integer> frameDurations = Collections.emptyList();
    private Map<String,Object> properties = Collections.emptyMap();
    
    TiledTile(int id) {
        this.id = id;
    }
    
    final void setInnerTagInfo(TiledImage image, List<TiledObject> collisionObjects,
            List<TiledTile> frames, List<Integer> frameDurations, Map<String,Object> properties) {
        this.image = image;
        if (collisionObjects != null) {
            this.collisionObjects = Collections.unmodifiableList(collisionObjects);
        }
        if (frames != null) {
            this.frames = Collections.unmodifiableList(frames);
            this.frameDurations = Collections.unmodifiableList(frameDurations);
        }
        if (properties != null) {
            this.properties = Collections.unmodifiableMap(properties);
        }
    }
    
    public final TiledTileset getTileset() {
        return tileset;
    }
    
    public final int getID() {
        return id;
    }
    
    public final int getTilesetX() {
        return tilesetX;
    }
    
    public final int getTilesetY() {
        return tilesetY;
    }
    
    /**
     * Returns this tile's image, if it is part of an image collection tileset,
     * or null if it is not.
     * @return This tile's image
     */
    public final TiledImage getImage() {
        return image;
    }
    
    /**
     * Returns this tile's object type, or null if it was not specified.
     * @return This tile's object type
     */
    public final String getType() {
        return type;
    }
    
    /**
     * Returns the terrain type of this tile's top left corner, or null if the
     * corner has no terrain type.
     * @return The terrain type of this tile's top left corner
     */
    public final TiledTerrainType getTopLeftTerrainType() {
        return terrainTypes[0];
    }
    
    /**
     * Returns the terrain type of this tile's top right corner, or null if the
     * corner has no terrain type.
     * @return The terrain type of this tile's top right corner
     */
    public final TiledTerrainType getTopRightTerrainType() {
        return terrainTypes[1];
    }
    
    /**
     * Returns the terrain type of this tile's bottom left corner, or null if
     * the corner has no terrain type.
     * @return The terrain type of this tile's bottom left corner
     */
    public final TiledTerrainType getBottomLeftTerrainType() {
        return terrainTypes[2];
    }
    
    /**
     * Returns the terrain type of this tile's bottom right corner, or null if
     * the corner has no terrain type.
     * @return The terrain type of this tile's bottom right corner
     */
    public final TiledTerrainType getBottomRightTerrainType() {
        return terrainTypes[3];
    }
    
    /**
     * Returns this tile's probability of being chosen over competing tiles by
     * the terrain tool, or -1 if it was not specified.
     * @return This tile's terrain tool probability
     */
    public final float getProbability() {
        return probability;
    }
    
    /**
     * Returns an unmodifiable List view of the objects that specify this tile's
     * collision properties. The list will be empty if no such objects were
     * specified.
     * @return The objects that specify this tile's collision properties
     */
    public final List<TiledObject> getCollisionObjects() {
        return collisionObjects;
    }
    
    /**
     * Returns the number of frames in this tile's animation, or 0 if this tile
     * is not animated.
     * @return The number of frames in this tile's animation
     */
    public final int getNumAnimationFrames() {
        return frames.size();
    }
    
    /**
     * Returns the tile displayed in this tile's animation frame at the
     * specified index (0 to getNumAnimationFrames() - 1 inclusive).
     * @param index The index of the frame to be returned
     * @return This tile's frame at the specified index
     * @throws UnsupportedOperationException if this tile is not animated
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public final TiledTile getAnimationFrame(int index) {
        if (frames.isEmpty()) {
            throw new UnsupportedOperationException("Attempted to get an animation frame of a non-animated"
                    + " TiledTile");
        }
        if (index < 0 || index >= frames.size()) {
            throw new IndexOutOfBoundsException("Attempted to get a TiledTile's animation frame at invalid"
                    + " index " + index);
        }
        return frames.get(index);
    }
    
    /**
     * Returns the duration in milliseconds of this tile's animation frame at
     * the specified index (0 to getNumAnimationFrames() - 1 inclusive).
     * @param index The index of the frame duration to be returned
     * @return The duration of this tile's frame at the specified index
     * @throws UnsupportedOperationException if this tile is not animated
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public final int getAnimationFrameDuration(int index) {
        if (frames.isEmpty()) {
            throw new UnsupportedOperationException("Attempted to get an animation frame duration of a"
                    + " non-animated TiledTile");
        }
        if (index < 0 || index >= frames.size()) {
            throw new IndexOutOfBoundsException("Attempted to get a TiledTile's animation frame duration at"
                    + " invalid index " + index);
        }
        return frameDurations.get(index);
    }
    
    /**
     * Returns an unmodifiable Map view of this tile's custom properties. Each
     * key in the Map is the name of a property, and its corresponding value is
     * the value of that property. The type of the value object corresponds
     * to the type of the property: String for a string property, Integer for an
     * int, Float for a float, Boolean for a bool, <code>java.awt.Color</code>
     * for a color, and <code>java.io.File</code> for a file.
     * @return This tile's custom properties
     */
    public final Map<String,Object> getProperties() {
        return properties;
    }
    
    /**
     * Returns the value of this tile's custom property with the specified name,
     * or null if no such property was specified. The type of the returned value
     * corresponds to the type of the property: String for a string property,
     * Integer for an int, Float for a float, Boolean for a bool, <code>
     * java.awt.Color</code> for a color, and <code>java.io.File</code> for a
     * file.
     * @param name The name of the property whose value is to be returned
     * @return The value of this tile's custom property with the specified name
     */
    public final Object getProperty(String name) {
        return properties.get(name);
    }
    
}
