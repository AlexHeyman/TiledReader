package org.tiledreader;

/**
 * <p>A TiledResource is an object of a type that can represent the "top-level"
 * structure of a Tiled file, and may have been read from such a structure by
 * a TiledReader. For instance, a TiledTileset may have been read from a
 * particular TSX file by a TiledReader's getTileset() method, and may thus
 * represent the top-level structure of that file. However, a TiledTileset may
 * instead represent a tileset embedded as a lower-level structure in a TMX
 * file.</p>
 * @author Alex Heyman
 */
public class TiledResource {
    
    private final String path;
    
    TiledResource(String path) {
        this.path = path;
    }
    
    /**
     * Returns the path to the Tiled file from which this resource was read as
     * the top-level structure, or null if this resource was read as a
     * lower-level structure (in particular, if it is a TiledTileset that
     * represents a tileset embedded in a TMX file). If this resource was
     * returned by a call to one of a TiledReader's resource getter methods
     * (e.g. getMap()), the String returned by this method may not be equal to
     * the path argument passed to the getter method. However, both path Strings
     * are guaranteed to point to the same file.
     * @return The path to the Tiled file from which this resource was read
     */
    public final String getPath() {
        return path;
    }
    
}
