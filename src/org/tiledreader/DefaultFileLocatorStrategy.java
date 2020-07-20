package org.tiledreader;

import java.io.*;

public class DefaultFileLocatorStrategy implements FileLocatorStrategy {
    @Override
    public InputStream openPath(String path) {
        try {
            return new FileInputStream(getCanonicalFile(path));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String resolvePath(String path, String basePath) {
        return getCanonicalFile(new File(basePath).toPath().resolveSibling(path).toString()).getPath();
    }

    private static File getCanonicalFile(String path) {
        try {
            return new File(path).getCanonicalFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
