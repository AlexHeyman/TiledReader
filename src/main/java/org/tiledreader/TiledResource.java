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
    
    private final TiledReader reader;
    private final String path;
    
    TiledResource(TiledReader reader, String path) {
        this.reader = reader;
        this.path = path;
    }
    
    /**
     * Returns the TiledReader that read this resource, whether or not it was
     * read from a Tiled file's top-level structure.
     * @return The TiledReader that read this resource
     */
    public final TiledReader getReader() {
        return reader;
    }
    
    /**
     * Returns the path to the Tiled file from which this resource was read as
     * the top-level structure, or null if this resource was read as a
     * lower-level structure. If it is not null, the path is an absolute path
     * returned by the getCanonicalPath() method of the TiledReader that read
     * this resource.
     * @return The path to the Tiled file from which this resource was read
     */
    public final String getPath() {
        return path;
    }
    
}
