package org.tiledreader;

import java.io.InputStream;

public interface FileLocatorStrategy {

    /**
     * Map all paths pointing to the same file to a single path representation. (e.g. relative paths to absolute path).
     *
     * @param path
     * @return
     */
    String sanitizePath(String path);

    /**
     * Opens an InputStream to the given path
     *
     * @param path
     * @return
     */
    InputStream openPath(String path);

    /**
     * Resolves a relative path
     *
     * @param path
     * @param basePath
     * @return
     */
    String resolvePath(String path, String basePath);
}
