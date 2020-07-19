package org.tiledreader;

import java.io.InputStream;

public interface FileLocatorStrategy {
    InputStream openPath(String path);
    String resolvePath(String path, String basePath);
}
