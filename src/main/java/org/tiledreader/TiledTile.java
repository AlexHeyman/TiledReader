package org.tiledreader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * <p>A TiledTile represents one of the tiles in a tileset. It corresponds to a
 * &lt;tile&gt; tag inside a &lt;tileset&gt; tag in a Tiled XML file, or to a
 * tile implicitly declared by the attributes of a &lt;tileset&gt; tag.</p>
 * @author Alex Heyman
 */
public class TiledTile implements TiledCustomizable {
    
    TiledTileset tileset = null;
    private final int id;
    int tilesetX = -1;
    int tilesetY = -1;
    private TiledImage image = null;
    String type = null;
    TiledObjectType typeInfo = null;
    float probability = -1;
    private List<TiledObject> collisionObjects = Collections.emptyList();
    private List<TiledTile> frames = Collections.emptyList();
    private List<Integer> frameDurations = Collections.emptyList();
    private Map<String,Object> nonDefaultProperties = Collections.emptyMap();
    private Map<String,Object> properties = Collections.emptyMap();
    
    TiledTile(int id) {
        this.id = id;
    }
    
    final void setInnerTagInfo(TiledImage image, List<TiledObject> collisionObjects,
            List<TiledTile> frames, List<Integer> frameDurations, Map<String,Object> nonDefaultProperties) {
        this.image = image;
        if (collisionObjects != null) {
            this.collisionObjects = Collections.unmodifiableList(collisionObjects);
        }
        if (frames != null) {
            this.frames = Collections.unmodifiableList(frames);
            this.frameDurations = Collections.unmodifiableList(frameDurations);
        }
        if (nonDefaultProperties != null) {
            this.nonDefaultProperties = Collections.unmodifiableMap(nonDefaultProperties);
        }
        
        List<Map<String,Object>> propertyTiers = new ArrayList<>();
        if (!this.nonDefaultProperties.isEmpty()) {
            propertyTiers.add(nonDefaultProperties);
        }
        if (typeInfo != null && !typeInfo.getProperties().isEmpty()) {
            propertyTiers.add(typeInfo.getProperties());
        }
        properties = new TieredMap<>(propertyTiers);
    }
    
    /**
     * Returns the tileset to which this tile belongs.
     * @return This tile's tileset
     */
    public final TiledTileset getTileset() {
        return tileset;
    }
    
    /**
     * Returns this tile's local ID within its tileset.
     * @return This tile's local ID
     */
    public final int getID() {
        return id;
    }
    
    /**
     * Returns this tile's x-coordinate within its tileset, if it is a
     * single-image tileset, or -1 if it is an image collection tileset.
     * @return This tile's x-coordinate within its tileset
     */
    public final int getTilesetX() {
        return tilesetX;
    }
    
    /**
     * Returns this tile's y-coordinate within its tileset, if it is a
     * single-image tileset, or -1 if it is an image collection tileset.
     * @return This tile's y-coordinate within its tileset
     */
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
     * Returns the object type information that determined the default values of
     * this tile's custom properties, or null if no such information was used.
     * @return The object type information that determined the default values of
     * this tile's custom properties
     */
    public final TiledObjectType getTypeInfo() {
        return typeInfo;
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
    
    @Override
    public final Map<String,Object> getProperties() {
        return properties;
    }
    
    @Override
    public final Object getProperty(String name) {
        return properties.get(name);
    }
    
    /**
     * Returns an unmodifiable Map view of this tile's custom properties that
     * were specified by the tile itself, rather than as defaults via its object
     * type. The format of the returned Map is the same as the format of the Map
     * returned by getProperties().
     * @return This tile's non-default custom properties
     */
    public final Map<String,Object> getNonDefaultProperties() {
        return nonDefaultProperties;
    }
    
}
