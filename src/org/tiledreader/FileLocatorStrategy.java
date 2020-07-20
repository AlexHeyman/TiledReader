package org.tiledreader;

import java.io.InputStream;

public interface FileLocatorStrategy {

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
