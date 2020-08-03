package org.tiledreader;

/**
 * <p>A TiledFile represents a file referenced as the value of one of a
 * TiledCustomizable's custom properties.</p>
 * @author Alex Heyman
 */
public class TiledFile {
    
    private final String path;
    
    TiledFile(String path) {
        this.path = path;
    }
    
    /**
     * Returns the path to this file. The path is an absolute path returned by
     * the getCanonicalPath() method of the TiledReader that read this file's
     * path.
     * @return The path to this file
     */
    public final String getPath() {
        return path;
    }
    
}
