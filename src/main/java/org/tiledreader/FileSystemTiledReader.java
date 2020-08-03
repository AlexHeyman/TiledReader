package org.tiledreader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>A FileSystemTiledReader is a type of TiledReader that locates and
 * identifies resources using standard paths through the local file system, and
 * caches resources in a HashMap with canonical path strings as keys. This is
 * the best type of TiledReader to use if you're not doing anything fancy with
 * file storage and not trying to integrate TiledReader with an existing asset
 * management system.</p>
 * @author Alex Heyman
 */
public class FileSystemTiledReader extends TiledReader {
    
    private final Map<String,TiledResource> resources = new HashMap<>();
    
    @Override
    public final String getCanonicalPath(String path) {
        try {
            return new File(path).getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public final String joinPaths(String basePath, String relativePath) {
        return new File(basePath).toPath().resolveSibling(relativePath).toString();
    }
    
    @Override
    public final InputStream getInputStream(String path) {
        try {
            return new FileInputStream(new File(path));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public final TiledResource getCachedResource(String path) {
        return resources.get(path);
    }
    
    @Override
    protected final void setCachedResource(String path, TiledResource resource) {
        resources.put(path, resource);
    }
    
    @Override
    protected final void removeCachedResource(String path) {
        resources.remove(path);
    }
    
    @Override
    protected final void clearCachedResources() {
        resources.clear();
    }
    
}
