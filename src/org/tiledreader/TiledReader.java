package org.tiledreader;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * <p>The TiledReader class is the main class of the TiledReader library, and
 * the library's means of accessing Tiled files. The TiledReader class cannot be
 * instantiated; instead, it has static methods that read Tiled maps, tilesets,
 * and object templates from specified files.</p>
 * 
 * <p>The TiledReader class stores pointers to the maps, tilesets, and object
 * templates corresponding to all of the files it has read, and will use these
 * pointers to return the very same resource object if asked to read the same
 * file multiple times. This is mainly to ensure that, if multiple Tiled maps
 * reference the same external tileset or object template file, the external
 * file will not be wastefully parsed and stored in memory multiple times.
 * However, the TiledReader class also contains static methods that can be
 * called manually to remove these pointers. Removing the pointer to a
 * no-longer-needed resource object is necessary to make the object vulnerable
 * to the Java garbage collector.</p>
 * 
 * <p>TiledReader does not support image data embedded directly in TMX/TSX
 * files. As of Tiled version 1.4.0, however, it is not possible to embed image
 * data in files using the Tiled editor.</p>
 * 
 * <p>TiledReader also ignores information in Tiled files pertaining to
 * deprecated or unsupported features of Tiled. These include the x and y
 * attributes of tile layers, object layers, and image layers (not to be
 * confused with the rendering x and y offsets, which are supported), as well as
 * the width and height attributes of object layers.</p>
 * 
 * <p>For details on the structure and content of Tiled files, see
 * <a href="https://doc.mapeditor.org/en/stable/reference/tmx-map-format/">the
 * official Tiled documentation on the subject</a>.</p>
 * @author Alex Heyman
 */
public final class TiledReader {
    
    private TiledReader() {}
    
    /**
     * The version number of TiledReader. Currently 1.0.2.
     */
    public static final String VERSION = "1.0.2";
    
    /**
     * The version number of Tiled that this version of TiledReader was designed
     * for. Currently 1.4.0.
     */
    public static final String TILED_VERSION = "1.4.0";
    
    /**
     * The version of the TMX file format that this version of TiledReader was
     * designed for. Currently 1.4. This is what matters most for file
     * compatibility, and TiledReader will produce a warning if it reads a file
     * with a TMX format version that does not match this constant.
     */
    public static final String TMX_VERSION = "1.4";
    
    private static Map<Integer,String> EVENT_TYPE_NAMES = null;
    
    private static void initEventTypeNames() {
        EVENT_TYPE_NAMES = new HashMap<>();
        for (Field f : XMLStreamConstants.class.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isStatic(mod) && Modifier.isPublic(mod) && Modifier.isFinal(mod)) {
                String name = f.getName();
                try {
                    EVENT_TYPE_NAMES.put((Integer)f.get(null), name);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("The value of the XML stream constant " + name
                            + " is inaccessible");
                }
            }
        }
    }
    
    private static String getEventTypeName(int eventType) {
        if (EVENT_TYPE_NAMES == null) {
            initEventTypeNames();
        }
        return EVENT_TYPE_NAMES.get(eventType);
    }
    
    private static final Map<String,Boolean> MAP_ATTRIBUTES = new HashMap<>();
    static {
        MAP_ATTRIBUTES.put("version", false);
        MAP_ATTRIBUTES.put("tiledversion", false);
        MAP_ATTRIBUTES.put("orientation", true);
        MAP_ATTRIBUTES.put("renderorder", false);
        MAP_ATTRIBUTES.put("compressionlevel", false);
        MAP_ATTRIBUTES.put("width", true);
        MAP_ATTRIBUTES.put("height", true);
        MAP_ATTRIBUTES.put("tilewidth", true);
        MAP_ATTRIBUTES.put("tileheight", true);
        MAP_ATTRIBUTES.put("hexsidelength", false);
        MAP_ATTRIBUTES.put("staggeraxis", false);
        MAP_ATTRIBUTES.put("staggerindex", false);
        MAP_ATTRIBUTES.put("backgroundcolor", false);
        MAP_ATTRIBUTES.put("nextlayerid", false);
        MAP_ATTRIBUTES.put("nextobjectid", false);
        MAP_ATTRIBUTES.put("infinite", false);
    }
    
    private static final Map<String,Boolean> TILESET_ATTRIBUTES = new HashMap<>();
    static {
        TILESET_ATTRIBUTES.put("name", true);
        TILESET_ATTRIBUTES.put("tilewidth", true);
        TILESET_ATTRIBUTES.put("tileheight", true);
        TILESET_ATTRIBUTES.put("spacing", false);
        TILESET_ATTRIBUTES.put("margin", false);
        TILESET_ATTRIBUTES.put("tilecount", true);
        TILESET_ATTRIBUTES.put("columns", true);
        TILESET_ATTRIBUTES.put("objectalignment", false);
    }
    
    private static final Map<String,Boolean> TMX_TILESET_ATTRIBUTES = new HashMap<>();
    static {
        for (String attribute : TILESET_ATTRIBUTES.keySet()) {
            TMX_TILESET_ATTRIBUTES.put(attribute, false);
        }
        TMX_TILESET_ATTRIBUTES.put("firstgid", true);
        TMX_TILESET_ATTRIBUTES.put("source", false);
    }
    
    private static final Map<String,Boolean> TSX_TILESET_ATTRIBUTES = new HashMap<>(TILESET_ATTRIBUTES);
    static {
        TSX_TILESET_ATTRIBUTES.put("version", false);
        TSX_TILESET_ATTRIBUTES.put("tiledversion", false);
    }
    
    private static final Map<String,Boolean> TX_TILESET_ATTRIBUTES = new HashMap<>();
    static {
        TX_TILESET_ATTRIBUTES.put("firstgid", true);
        TX_TILESET_ATTRIBUTES.put("source", true);
    }
    
    private static final Map<String,Boolean> TILEOFFSET_ATTRIBUTES = new HashMap<>();
    static {
        TILEOFFSET_ATTRIBUTES.put("x", true);
        TILEOFFSET_ATTRIBUTES.put("y", true);
    }
    
    private static final Map<String,Boolean> GRID_ATTRIBUTES = new HashMap<>();
    static {
        GRID_ATTRIBUTES.put("orientation", true);
        GRID_ATTRIBUTES.put("width", true);
        GRID_ATTRIBUTES.put("height", true);
    }
    
    private static final Map<String,Boolean> IMAGE_ATTRIBUTES = new HashMap<>();
    static {
        IMAGE_ATTRIBUTES.put("format", false);
        IMAGE_ATTRIBUTES.put("id", false);
        IMAGE_ATTRIBUTES.put("source", false);
        IMAGE_ATTRIBUTES.put("trans", false);
        IMAGE_ATTRIBUTES.put("width", false);
        IMAGE_ATTRIBUTES.put("height", false);
    }
    
    private static final Map<String,Boolean> TERRAIN_ATTRIBUTES = new HashMap<>();
    static {
        TERRAIN_ATTRIBUTES.put("name", true);
        TERRAIN_ATTRIBUTES.put("tile", true);
    }
    
    private static final Map<String,Boolean> TILESET_TILE_ATTRIBUTES = new HashMap<>();
    static {
        TILESET_TILE_ATTRIBUTES.put("id", true);
        TILESET_TILE_ATTRIBUTES.put("type", false);
        TILESET_TILE_ATTRIBUTES.put("terrain", false);
        TILESET_TILE_ATTRIBUTES.put("probability", false);
    }
    
    private static final Map<String,Boolean> FRAME_ATTRIBUTES = new HashMap<>();
    static {
        FRAME_ATTRIBUTES.put("tileid", true);
        FRAME_ATTRIBUTES.put("duration", true);
    }
    
    private static final Map<String,Boolean> WANGSET_ATTRIBUTES = new HashMap<>();
    static {
        WANGSET_ATTRIBUTES.put("name", true);
        WANGSET_ATTRIBUTES.put("tile", true);
    }
    
    private static final Map<String,Boolean> WANG_COLOR_ATTRIBUTES = new HashMap<>();
    static {
        WANG_COLOR_ATTRIBUTES.put("name", true);
        WANG_COLOR_ATTRIBUTES.put("color", true);
        WANG_COLOR_ATTRIBUTES.put("tile", true);
        WANG_COLOR_ATTRIBUTES.put("probability", true);
    }
    
    private static final Map<String,Boolean> WANGTILE_ATTRIBUTES = new HashMap<>();
    static {
        WANGTILE_ATTRIBUTES.put("tileid", true);
        WANGTILE_ATTRIBUTES.put("wangid", true);
    }
    
    private static final Map<String,Boolean> LAYER_ATTRIBUTES = new HashMap<>();
    static {
        LAYER_ATTRIBUTES.put("id", true);
        LAYER_ATTRIBUTES.put("name", true);
        LAYER_ATTRIBUTES.put("x", false);
        LAYER_ATTRIBUTES.put("y", false);
        LAYER_ATTRIBUTES.put("width", true);
        LAYER_ATTRIBUTES.put("height", true);
        LAYER_ATTRIBUTES.put("opacity", false);
        LAYER_ATTRIBUTES.put("visible", false);
        LAYER_ATTRIBUTES.put("tintcolor", false);
        LAYER_ATTRIBUTES.put("offsetx", false);
        LAYER_ATTRIBUTES.put("offsety", false);
    }
    
    private static final Map<String,Boolean> DATA_ATTRIBUTES = new HashMap<>();
    static {
        DATA_ATTRIBUTES.put("encoding", false);
        DATA_ATTRIBUTES.put("compression", false);
    }
    
    private static final Map<String,Boolean> CHUNK_ATTRIBUTES = new HashMap<>();
    static {
        CHUNK_ATTRIBUTES.put("x", true);
        CHUNK_ATTRIBUTES.put("y", true);
        CHUNK_ATTRIBUTES.put("width", true);
        CHUNK_ATTRIBUTES.put("height", true);
    }
    
    private static final Map<String,Boolean> DATA_TILE_ATTRIBUTES = new HashMap<>();
    static {
        DATA_TILE_ATTRIBUTES.put("gid", false);
    }
    
    private static final Map<String,Boolean> MAP_OBJECTGROUP_ATTRIBUTES = new HashMap<>();
    static {
        MAP_OBJECTGROUP_ATTRIBUTES.put("id", true);
        MAP_OBJECTGROUP_ATTRIBUTES.put("name", true);
        MAP_OBJECTGROUP_ATTRIBUTES.put("color", false);
        MAP_OBJECTGROUP_ATTRIBUTES.put("x", false);
        MAP_OBJECTGROUP_ATTRIBUTES.put("y", false);
        MAP_OBJECTGROUP_ATTRIBUTES.put("width", false);
        MAP_OBJECTGROUP_ATTRIBUTES.put("height", false);
        MAP_OBJECTGROUP_ATTRIBUTES.put("opacity", false);
        MAP_OBJECTGROUP_ATTRIBUTES.put("visible", false);
        MAP_OBJECTGROUP_ATTRIBUTES.put("tintcolor", false);
        MAP_OBJECTGROUP_ATTRIBUTES.put("offsetx", false);
        MAP_OBJECTGROUP_ATTRIBUTES.put("offsety", false);
        MAP_OBJECTGROUP_ATTRIBUTES.put("draworder", false);
    }
    
    private static final Map<String,Boolean> TILE_OBJECTGROUP_ATTRIBUTES = new HashMap<>();
    static {
        for (String attribute : MAP_OBJECTGROUP_ATTRIBUTES.keySet()) {
            MAP_OBJECTGROUP_ATTRIBUTES.put(attribute, false);
        }
    }
    
    private static final Map<String,Boolean> TEMPLATE_OBJECT_ATTRIBUTES = new HashMap<>();
    static {
        TEMPLATE_OBJECT_ATTRIBUTES.put("name", false);
        TEMPLATE_OBJECT_ATTRIBUTES.put("type", false);
        TEMPLATE_OBJECT_ATTRIBUTES.put("width", false);
        TEMPLATE_OBJECT_ATTRIBUTES.put("height", false);
        TEMPLATE_OBJECT_ATTRIBUTES.put("rotation", false);
        TEMPLATE_OBJECT_ATTRIBUTES.put("gid", false);
        TEMPLATE_OBJECT_ATTRIBUTES.put("visible", false);
    }
    
    private static final Map<String,Boolean> OBJECT_ATTRIBUTES = new HashMap<>();
    static {
        for (String attribute : TEMPLATE_OBJECT_ATTRIBUTES.keySet()) {
            OBJECT_ATTRIBUTES.put(attribute, false);
        }
        OBJECT_ATTRIBUTES.put("id", true);
        OBJECT_ATTRIBUTES.put("x", true);
        OBJECT_ATTRIBUTES.put("y", true);
        OBJECT_ATTRIBUTES.put("template", false);
    }
    
    private static final Map<String,Boolean> TEXT_ATTRIBUTES = new HashMap<>();
    static {
        TEXT_ATTRIBUTES.put("fontfamily", false);
        TEXT_ATTRIBUTES.put("pixelsize", false);
        TEXT_ATTRIBUTES.put("wrap", false);
        TEXT_ATTRIBUTES.put("color", false);
        TEXT_ATTRIBUTES.put("bold", false);
        TEXT_ATTRIBUTES.put("italic", false);
        TEXT_ATTRIBUTES.put("underline", false);
        TEXT_ATTRIBUTES.put("strikeout", false);
        TEXT_ATTRIBUTES.put("kerning", false);
        TEXT_ATTRIBUTES.put("halign", false);
        TEXT_ATTRIBUTES.put("valign", false);
    }
    
    private static final Map<String,Boolean> IMAGELAYER_ATTRIBUTES = new HashMap<>();
    static {
        IMAGELAYER_ATTRIBUTES.put("id", true);
        IMAGELAYER_ATTRIBUTES.put("name", true);
        IMAGELAYER_ATTRIBUTES.put("x", false);
        IMAGELAYER_ATTRIBUTES.put("y", false);
        IMAGELAYER_ATTRIBUTES.put("opacity", false);
        IMAGELAYER_ATTRIBUTES.put("visible", false);
        IMAGELAYER_ATTRIBUTES.put("tintcolor", false);
        IMAGELAYER_ATTRIBUTES.put("offsetx", false);
        IMAGELAYER_ATTRIBUTES.put("offsety", false);
    }
    
    private static final Map<String,Boolean> GROUP_ATTRIBUTES = new HashMap<>();
    static {
        GROUP_ATTRIBUTES.put("id", true);
        GROUP_ATTRIBUTES.put("name", true);
        GROUP_ATTRIBUTES.put("opacity", false);
        GROUP_ATTRIBUTES.put("visible", false);
        GROUP_ATTRIBUTES.put("tintcolor", false);
        GROUP_ATTRIBUTES.put("offsetx", false);
        GROUP_ATTRIBUTES.put("offsety", false);
    }
    
    private static final Map<String,Boolean> PROPERTY_ATTRIBUTES = new HashMap<>();
    static {
        PROPERTY_ATTRIBUTES.put("name", true);
        PROPERTY_ATTRIBUTES.put("type", false);
        PROPERTY_ATTRIBUTES.put("value", false);
    }
    
    private static final Color DEFAULT_TINT_COLOR = new Color(1f, 1f, 1f, 1f);
    
    private static final int FL_TILE_FLIPX = 0x80000000;
    private static final int FL_TILE_FLIPY = 0x40000000;
    private static final int FL_TILE_FLIPD = 0x20000000;
    private static final int FL_TILE_ALL = FL_TILE_FLIPX | FL_TILE_FLIPY | FL_TILE_FLIPD;
    
    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    static {
        LOGGER.setLevel(Level.INFO);
    }
    
    private static final Base64.Decoder BASE_64_DECODER = Base64.getDecoder();
    
    private static class ResourceData {
        
        private TiledResource resource = null;
        private final Set<File> referToThis = new HashSet<>();
        private final Set<File> referencedByThis = new HashSet<>();
        
    }
    
    private static final Map<File,ResourceData> resources = new HashMap<>();
    
    private static File getCanonicalFile(String path) {
        try {
            return new File(path).getCanonicalFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static void ensureReference(File referer, File referent) {
        resources.get(referer).referencedByThis.add(referent);
        resources.get(referent).referToThis.add(referer);
    }
    
    /**
     * Reads a Tiled map from the specified TMX file and returns it as a
     * TiledMap object. If the Tiled map references any tilesets or object
     * templates in external files, those files will be automatically read as
     * well.
     * @param path The path to the TMX file to read
     * @return The Tiled map from the specified file
     */
    public static TiledMap getMap(String path) {
        if (!path.endsWith(".tmx")) {
            throw new RuntimeException("Attempted to read a Tiled map from a path that does not point to a"
                    + " TMX file: " + path);
        }
        File file = getCanonicalFile(path);
        path = file.getPath();
        ResourceData data = resources.get(file);
        if (data == null) {
            data = new ResourceData();
            resources.put(file, data);
            XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty("javax.xml.stream.isCoalescing", true);
            try {
                XMLStreamReader reader = factory.createXMLStreamReader(new FileInputStream(file));
                LOGGER.log(Level.INFO, "Beginning to parse TMX file: {0}", path);
                if (reader.getEventType() == XMLStreamConstants.START_DOCUMENT) {
                    next(reader);
                }
                OUTER: while (true) {
                    switch (reader.getEventType()) {
                        case XMLStreamConstants.START_ELEMENT:
                            switch (reader.getLocalName()) {
                                case "map":
                                    if (data.resource == null) {
                                        data.resource = readMap(file, reader);
                                    } else {
                                        ignoreRedundantTag(reader);
                                    }
                                    break;
                                default:
                                    ignoreUnexpectedTag(reader);
                                    break;
                            }
                            break;
                        case XMLStreamConstants.END_DOCUMENT:
                            break OUTER;
                        default:
                            ignoreUnexpectedEvent(reader);
                            break;
                    }
                    next(reader);
                }
                if (data.resource == null) {
                    throw new XMLStreamException("TMX file (" + path + ") contains no top-level <map> tag");
                }
                LOGGER.log(Level.INFO, "Finished parsing TMX file: {0}", path);
            } catch (FileNotFoundException | XMLStreamException e) {
                throw new RuntimeException(e);
            }
        }
        return (TiledMap)(data.resource);
    }
    
    /**
     * Reads a Tiled tileset from the specified TSX file and returns it as a
     * TiledTileset object. If the tileset references any object templates in
     * external files, those files will be automatically read as well.
     * @param path The path to the TSX file to read
     * @return The Tiled tileset from the specified file
     */
    public static TiledTileset getTileset(String path) {
        if (!path.endsWith(".tsx")) {
            throw new RuntimeException("Attempted to read a Tiled tileset from a path that does not point to"
                    + " a TSX file: " + path);
        }
        File file = getCanonicalFile(path);
        path = file.getPath();
        ResourceData data = resources.get(file);
        if (data == null) {
            data = new ResourceData();
            resources.put(file, data);
            XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty("javax.xml.stream.isCoalescing", true);
            try {
                XMLStreamReader reader = factory.createXMLStreamReader(new FileInputStream(file));
                LOGGER.log(Level.INFO, "Beginning to parse TSX file: {0}", path);
                if (reader.getEventType() == XMLStreamConstants.START_DOCUMENT) {
                    next(reader);
                }
                OUTER: while (true) {
                    switch (reader.getEventType()) {
                        case XMLStreamConstants.START_ELEMENT:
                            switch (reader.getLocalName()) {
                                case "tileset":
                                    if (data.resource == null) {
                                        Map<String,String> attributeValues
                                                = getAttributeValues(reader, TSX_TILESET_ATTRIBUTES);
                                        checkVersion(reader, attributeValues.get("version"));
                                        data.resource = readTileset(
                                                file, true, reader, attributeValues, null);
                                    } else {
                                        ignoreRedundantTag(reader);
                                    }
                                    break;
                                default:
                                    ignoreUnexpectedTag(reader);
                                    break;
                            }
                            break;
                        case XMLStreamConstants.END_DOCUMENT:
                            break OUTER;
                        default:
                            ignoreUnexpectedEvent(reader);
                            break;
                    }
                    next(reader);
                }
                if (data.resource == null) {
                    throw new XMLStreamException("TSX file (" + path
                            + ") contains no top-level <tileset> tag");
                }
                LOGGER.log(Level.INFO, "Finished parsing TSX file: {0}", path);
            } catch (FileNotFoundException | XMLStreamException e) {
                throw new RuntimeException(e);
            }
        }
        return (TiledTileset)(data.resource);
    }
    
    /**
     * Reads a Tiled object template from the specified TX file and returns it
     * as a TiledObjectTemplate object. If the template references a tileset in
     * an external file, that file will be automatically read as well.
     * @param path The path to the TX file to read
     * @return The Tiled object template from the specified file
     */
    public static TiledObjectTemplate getTemplate(String path) {
        if (!path.endsWith(".tx")) {
            throw new RuntimeException("Attempted to read a Tiled object template from a path that does not"
                    + " point to a TX file: " + path);
        }
        File file = getCanonicalFile(path);
        path = file.getPath();
        ResourceData data = resources.get(file);
        if (data == null) {
            data = new ResourceData();
            resources.put(file, data);
            XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty("javax.xml.stream.isCoalescing", true);
            try {
                XMLStreamReader reader = factory.createXMLStreamReader(new FileInputStream(file));
                LOGGER.log(Level.INFO, "Beginning to parse TX file: {0}", path);
                if (reader.getEventType() == XMLStreamConstants.START_DOCUMENT) {
                    next(reader);
                }
                OUTER: while (true) {
                    switch (reader.getEventType()) {
                        case XMLStreamConstants.START_ELEMENT:
                            switch (reader.getLocalName()) {
                                case "template":
                                    if (data.resource == null) {
                                        data.resource = readTemplate(file, reader);
                                    } else {
                                        ignoreRedundantTag(reader);
                                    }
                                    break;
                                default:
                                    ignoreUnexpectedTag(reader);
                                    break;
                            }
                            break;
                        case XMLStreamConstants.END_DOCUMENT:
                            break OUTER;
                        default:
                            ignoreUnexpectedEvent(reader);
                            break;
                    }
                    next(reader);
                }
                if (data.resource == null) {
                    throw new XMLStreamException("TX file (" + path
                            + ") contains no top-level <template> tag");
                }
                LOGGER.log(Level.INFO, "Finished parsing TX file: {0}", path);
            } catch (FileNotFoundException | XMLStreamException e) {
                throw new RuntimeException(e);
            }
        }
        return (TiledObjectTemplate)(data.resource);
    }
    
    /**
     * Removes the TiledReader class' pointer to the resource it read from the
     * specified file, if it has read that file before.
     * @param path The path to the file to forget about
     * @param cleanUp If true, also remove the pointers to all of the resources
     * referenced by the resource from the specified file, and not referenced by
     * any of the other resources that the TiledReader class still points to.
     * This parameter applies recursively, so if the removal of any of these
     * "orphaned" resources causes more resources to be orphaned, those will be
     * removed as well.
     * @return Whether the specified file had been read before this method was
     * called, and hence whether the removal occurred
     */
    public static boolean removeResource(String path, boolean cleanUp) {
        return removeResource(getCanonicalFile(path), cleanUp);
    }
    
    private static boolean removeResource(File file, boolean cleanUp) {
        ResourceData data = resources.get(file);
        if (data == null) {
            return false;
        }
        for (File refererFile : data.referToThis) {
            resources.get(refererFile).referencedByThis.remove(file);
        }
        if (cleanUp) {
            List<File> orphanedFiles = new ArrayList<>();
            for (File referencedFile : data.referencedByThis) {
                ResourceData referencedData = resources.get(referencedFile);
                referencedData.referToThis.remove(file);
                if (referencedData.referToThis.isEmpty()) {
                    orphanedFiles.add(referencedFile);
                }
            }
            for (File orphanedFile : orphanedFiles) {
                removeResource(orphanedFile, true);
            }
        } else {
            for (File referencedFile : data.referencedByThis) {
                resources.get(referencedFile).referToThis.remove(file);
            }
        }
        resources.remove(file);
        return true;
    }
    
    /**
     * Removes all of the TiledReader class' pointers to resources that it has
     * read from files.
     */
    public static void clearResources() {
        resources.clear();
    }
    
    private static String describeReaderLocation(XMLStreamReader reader) {
        Location location = reader.getLocation();
        return "(line " + (location.getLineNumber() == -1 ? "unknown" : location.getLineNumber())
                + ", column "
                + (location.getColumnNumber() == -1 ? "unknown" : location.getColumnNumber()) + ")";
    }
    
    private static void next(XMLStreamReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            reader.next();
            int readType = reader.getEventType();
            if (readType != XMLStreamConstants.COMMENT && !reader.isWhiteSpace()) {
                return;
            }
        }
        throw new XMLStreamException(describeReaderLocation(reader) + ": Unexpected end of file");
    }
    
    private static void checkEndTagMatch(XMLStreamReader reader, String startTagName)
            throws XMLStreamException {
        if (!reader.getLocalName().equals(startTagName)) {
            throw new XMLStreamException(describeReaderLocation(reader) + ": End tag (<"
                    + reader.getLocalName() + ">) mismatched with start tag (<" + startTagName + ">)");
        }
    }
    
    private static void skipTag(XMLStreamReader reader) throws XMLStreamException {
        String startTagName = reader.getLocalName();
        next(reader);
        while (reader.getEventType() != XMLStreamConstants.END_ELEMENT) {
            if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                skipTag(reader);
            }
            next(reader);
        }
        checkEndTagMatch(reader, startTagName);
    }
    
    private static void ignoreUnexpectedEvent(XMLStreamReader reader) throws XMLStreamException {
        LOGGER.log(Level.WARNING, "{0}: Ignoring unexpected XML parsing event type ({1})",
                new Object[]{describeReaderLocation(reader), getEventTypeName(reader.getEventType())});
        if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
            skipTag(reader);
        }
    }
    
    private static void ignoreUnexpectedTag(XMLStreamReader reader) throws XMLStreamException {
        LOGGER.log(Level.WARNING, "{0}: Ignoring unexpected XML tag name (<{1}>)",
                new Object[]{describeReaderLocation(reader), reader.getLocalName()});
        skipTag(reader);
    }
    
    private static void ignoreRedundantTag(XMLStreamReader reader) throws XMLStreamException {
        LOGGER.log(Level.WARNING, "{0}: Ignoring redundant XML tag (<{1}>)",
                new Object[]{describeReaderLocation(reader), reader.getLocalName()});
        skipTag(reader);
    }
    
    private static void finishTag(XMLStreamReader reader) throws XMLStreamException {
        String startTagName = reader.getLocalName();
        next(reader);
        while (reader.getEventType() != XMLStreamConstants.END_ELEMENT) {
            if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                ignoreUnexpectedTag(reader);
            } else {
                ignoreUnexpectedEvent(reader);
            }
            next(reader);
        }
        checkEndTagMatch(reader, startTagName);
    }
    
    private static void throwMissingTagException(XMLStreamReader reader, String tag)
            throws XMLStreamException {
        throw new XMLStreamException(describeReaderLocation(reader) + ": Missing required tag (<" + tag
                + ">) in outer tag (<" + reader.getLocalName() + ">)");
    }
    
    private static void giveUnexpectedAttributeWarning(XMLStreamReader reader, String attribute) {
        LOGGER.log(Level.WARNING, "{0}: Ignoring unexpected tag attribute ({1})",
                new Object[]{describeReaderLocation(reader), attribute});
    }
    
    private static void giveRedundantAttributeWarning(XMLStreamReader reader, String attribute) {
        LOGGER.log(Level.WARNING, "{0}: Ignoring redundant tag attribute ({1})",
                new Object[]{describeReaderLocation(reader), attribute});
    }
    
    private static void throwMissingAttributeException(XMLStreamReader reader, String attribute)
            throws XMLStreamException {
        throw new XMLStreamException(describeReaderLocation(reader) + ": Missing required tag attribute ("
                + attribute + ")");
    }
    
    private static Map<String,String> getAttributeValues(XMLStreamReader reader,
            Map<String,Boolean> attributes) throws XMLStreamException {
        Map<String,String> attributeValues = new HashMap<>();
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String attribute = reader.getAttributeLocalName(i);
            if (attributes.containsKey(attribute)) {
                if (attributeValues.containsKey(attribute)) {
                    giveRedundantAttributeWarning(reader, attribute);
                } else {
                    attributeValues.put(attribute, reader.getAttributeValue(i));
                }
            } else {
                giveUnexpectedAttributeWarning(reader, attribute);
            }
        }
        for (Map.Entry<String,Boolean> entry : attributes.entrySet()) {
            if (entry.getValue() && !attributeValues.containsKey(entry.getKey())) {
                throwMissingAttributeException(reader, entry.getKey());
            }
        }
        return attributeValues;
    }
    
    private static void throwInvalidValueException(XMLStreamReader reader, String attribute, Object value,
            String message) throws XMLStreamException {
        throw new XMLStreamException(describeReaderLocation(reader) + ": <" + reader.getLocalName()
                + "> tag's " + attribute + " attribute has invalid value " + value + " - " + message);
    }
    
    private static void checkVersion(XMLStreamReader reader, String version) {
        if (version == null) {
            LOGGER.log(Level.WARNING, "{0}: No TMX format version specified (expected version: {1})."
                    + " Parsing errors may occur.",
                    new Object[]{describeReaderLocation(reader), TMX_VERSION});
        } else if (!version.equals(TMX_VERSION)) {
            LOGGER.log(Level.WARNING, "{0}: Specified TMX format version ({1}) does not match expected"
                    + " version ({2}). Parsing errors may occur.",
                    new Object[]{describeReaderLocation(reader), version, TMX_VERSION});
        }
    }
    
    private static int parseInt(XMLStreamReader reader, String attribute, String value,
            boolean useDefault, int default_) throws XMLStreamException {
        if (value == null) {
            if (useDefault) {
                return default_;
            } else {
                throwMissingAttributeException(reader, attribute);
            }
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throwInvalidValueException(reader, attribute, value, "value must be integer");
            return 0;
        }
    }
    
    private static int parseInt(XMLStreamReader reader, String attribute, String value)
            throws XMLStreamException {
        return parseInt(reader, attribute, value, false, 0);
    }
    
    private static float parseFloat(XMLStreamReader reader, String attribute, String value,
            boolean useDefault, float default_) throws XMLStreamException {
        if (value == null) {
            if (useDefault) {
                return default_;
            } else {
                throwMissingAttributeException(reader, attribute);
            }
        }
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            throwInvalidValueException(reader, attribute, value, "value must be float");
            return 0;
        }
    }
    
    private static float parseFloat(XMLStreamReader reader, String attribute, String value)
            throws XMLStreamException {
        return parseFloat(reader, attribute, value, false, 0);
    }
    
    private static boolean parseBoolean(XMLStreamReader reader, String attribute, String value,
            boolean useDefault, boolean default_) throws XMLStreamException {
        if (value == null) {
            if (useDefault) {
                return default_;
            } else {
                throwMissingAttributeException(reader, attribute);
                return false;
            }
        } else switch (value) {
            case "true": return true;
            case "false": return false;
            default: throwInvalidValueException(reader, attribute, value, "value must be 'true' or 'false'");
                return false;
        }
    }
    
    private static boolean parseBoolean(XMLStreamReader reader, String attribute, String value)
            throws XMLStreamException {
        return parseBoolean(reader, attribute, value, false, false);
    }
    
    private static boolean parseBooleanFromInt(XMLStreamReader reader, String attribute, String value,
            boolean useDefault, boolean default_) throws XMLStreamException {
        if (value == null) {
            if (useDefault) {
                return default_;
            } else {
                throwMissingAttributeException(reader, attribute);
            }
        }
        int intValue;
        try {
            intValue = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throwInvalidValueException(reader, attribute, value, "value must be 0 or 1");
            return false;
        }
        switch (intValue) {
            case 1: return true;
            case 0: return false;
            default: throwInvalidValueException(reader, attribute, value, "value must be 0 or 1");
                return false;
        }
    }
    
    private static boolean parseBooleanFromInt(XMLStreamReader reader, String attribute, String value)
            throws XMLStreamException {
        return parseBooleanFromInt(reader, attribute, value, false, false);
    }
    
    private static <E extends Enum<E>> E parseEnumValue(Class<E> cls, XMLStreamReader reader,
            String attribute, String value, boolean useDefault, E default_) throws XMLStreamException {
        if (value == null) {
            if (useDefault) {
                return default_;
            } else {
                throwMissingAttributeException(reader, attribute);
            }
        }
        try {
            return (E)(cls.getMethod("valueOf", String.class).invoke(null, value.toUpperCase()));
        } catch (IllegalArgumentException e) {
            String[] enumValues;
            try {
                enumValues = (String[])cls.getMethod("values").invoke(null);
            } catch (IllegalAccessException | InvocationTargetException
                    | NoSuchMethodException | SecurityException e2) {
                throw new RuntimeException(e2);
            }
            throwInvalidValueException(reader, attribute, value,
                    "value must be one of the following: " + String.join(", ", enumValues));
            return null;
        } catch (IllegalAccessException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static <E extends Enum<E>> E parseEnumValue(Class<E> cls, XMLStreamReader reader,
            String attribute, String value) throws XMLStreamException {
        return parseEnumValue(cls, reader, attribute, value, false, null);
    }
    
    private static int parseHexDigit(char digitChar) {
        char upperChar = Character.toUpperCase(digitChar);
        if (upperChar >= '0' && upperChar <= '9') {
            return upperChar - '0';
        } else if (upperChar >= 'A' && upperChar <= 'F') {
            return upperChar - 'A' + 10;
        } else {
            return -1;
        }
    }
    
    private static Color parseColor(XMLStreamReader reader, String attribute, String value,
            boolean useDefault, Color default_) throws XMLStreamException {
        if (value == null) {
            if (useDefault) {
                return default_;
            } else {
                throwMissingAttributeException(reader, attribute);
            }
        }
        if (value.length() >= 1) {
            String hexDigits = (value.charAt(0) == '#' ? value.substring(1) : value);
            if (hexDigits.length() == 6 || hexDigits.length() == 8) {
                int[] values = new int[hexDigits.length()/2];
                for (int i = 0; i < values.length; i++) {
                    int upper = parseHexDigit(hexDigits.charAt(2*i));
                    int lower = parseHexDigit(hexDigits.charAt(2*i + 1));
                    if (upper == -1 || lower == -1) {
                        throwInvalidValueException(reader, attribute, value,
                                "value must be a valid #RRGGBB or #AARRGGBB color code");
                    }
                    values[i] = (upper << 4) + lower;
                }
                if (values.length == 3) {
                    return new Color(values[0], values[1], values[2]);
                } else {
                    return new Color(values[1], values[2], values[3], values[0]);
                }
            }
        }
        throwInvalidValueException(reader, attribute, value,
                "value must be a valid #RRGGBB or #AARRGGBB color code");
        return null;
    }
    
    private static Color parseColor(XMLStreamReader reader, String attribute, String value)
            throws XMLStreamException {
        return parseColor(reader, attribute, value, false, null);
    }
    
    private static File parseFile(File file, XMLStreamReader reader, String attribute, String value,
            boolean useDefault, File default_) throws XMLStreamException {
        if (value == null) {
            if (useDefault) {
                return default_;
            } else {
                throwMissingAttributeException(reader, attribute);
            }
        }
        return getCanonicalFile(file.toPath().resolveSibling(value).toString());
    }
    
    private static File parseFile(File file, XMLStreamReader reader, String attribute, String value)
            throws XMLStreamException {
        return parseFile(file, reader, attribute, value, false, null);
    }
    
    private static class ReferencedTileset {
        
        private final TiledTileset tileset;
        private final int firstGID;
        
        private ReferencedTileset(TiledTileset tileset, int firstGID) {
            this.tileset = tileset;
            this.firstGID = firstGID;
        }
        
    }
    
    private static class MapTileData {
        
        private List<ReferencedTileset> tilesets = new ArrayList<>();
        private Map<Integer,TiledTile> gidTiles = null;
        
        private void initGIDTiles() throws XMLStreamException {
            gidTiles = new HashMap<>();
            for (ReferencedTileset referencedTileset : tilesets) {
                TiledTileset tileset = referencedTileset.tileset;
                int firstGID = referencedTileset.firstGID;
                for (TiledTile tile : tileset.getTiles()) {
                    int id = tile.getID();
                    int gid = firstGID + id;
                    if (gidTiles.put(gid, tile) != null) {
                        throw new XMLStreamException("Tileset with first global ID " + firstGID
                                + " contains a tile with local ID " + id + " and thus global ID " + gid
                                + ", conflicting with a tile with that same global ID from another tileset");
                    }
                }
            }
        }
        
    }
    
    private static class PropertyObjectData {
        
        private final Map<String,Object> properties;
        private final String propertyName;
        private final int objectID;
        
        private PropertyObjectData(Map<String,Object> properties, String propertyName, int objectID) {
            this.properties = properties;
            this.propertyName = propertyName;
            this.objectID = objectID;
        }
        
    }
    
    private static class TileObjectToResolve {
        
        private final TiledObject object;
        private final int gid;
        
        private TileObjectToResolve(TiledObject object, int gid) {
            this.object = object;
            this.gid = gid;
        }
        
    }
    
    private static TiledMap readMap(File file, XMLStreamReader reader) throws XMLStreamException {
        Map<String,String> attributeValues = getAttributeValues(reader, MAP_ATTRIBUTES);
        
        String version = attributeValues.get("version");
        checkVersion(reader, version);
        
        TiledMap.Orientation orientation = parseEnumValue(TiledMap.Orientation.class,
                reader, "orientation", attributeValues.get("orientation"));
        
        String renderOrderStr = attributeValues.get("renderorder");
        TiledMap.RenderOrder renderOrder = (renderOrderStr == null ? TiledMap.RenderOrder.RIGHT_DOWN
                : parseEnumValue(TiledMap.RenderOrder.class,
                reader, "renderorder", renderOrderStr.replace('-', '_')));
        
        int compressionLevel = parseInt(
                reader, "compressionlevel", attributeValues.get("compressionlevel"), true, -1);
        int width = parseInt(reader, "width", attributeValues.get("width"));
        int height = parseInt(reader, "height", attributeValues.get("height"));
        int tileWidth = parseInt(reader, "tilewidth", attributeValues.get("tilewidth"));
        int tileHeight = parseInt(reader, "tileheight", attributeValues.get("tileheight"));
        
        String hexSideLengthStr = attributeValues.get("hexsidelength");
        int hexSideLength = -1;
        if (orientation == TiledMap.Orientation.HEXAGONAL) {
            hexSideLength = parseInt(reader, "hexsidelength", hexSideLengthStr);
        } else if (hexSideLengthStr != null) {
            giveUnexpectedAttributeWarning(reader, "hexsidelength");
        }
        
        String staggerAxisStr = attributeValues.get("staggeraxis");
        String staggerIndexStr = attributeValues.get("staggerindex");
        TiledMap.StaggerAxis staggerAxis = null;
        TiledMap.StaggerIndex staggerIndex = null;
        boolean staggerAtttributesRequired = (orientation == TiledMap.Orientation.STAGGERED
                || orientation == TiledMap.Orientation.HEXAGONAL);
        if (staggerAtttributesRequired) {
            staggerAxis = parseEnumValue(TiledMap.StaggerAxis.class,
                    reader, "staggeraxis", staggerAxisStr);
            staggerIndex = parseEnumValue(TiledMap.StaggerIndex.class,
                    reader, "staggerindex", staggerIndexStr);
        } else {
            if (staggerAxisStr != null) {
                giveUnexpectedAttributeWarning(reader, "staggeraxis");
            }
            if (staggerIndexStr != null) {
                giveUnexpectedAttributeWarning(reader, "staggerindex");
            }
        }
        
        Color backgroundColor = parseColor(
                reader, "backgroundcolor", attributeValues.get("backgroundcolor"), true, null);
        int nextLayerID = parseInt(reader, "nextlayerid", attributeValues.get("nextlayerid"), true, -1);
        int nextObjectID = parseInt(reader, "nextobjectid", attributeValues.get("nextobjectid"), true, -1);
        boolean infinite = parseBooleanFromInt(
                reader, "infinite", attributeValues.get("infinite"), true, false);
        
        Map<String,Object> properties = null;
        MapTileData tileData = new MapTileData();
        List<TiledLayer> topLevelLayers = new ArrayList<>();
        List<TiledLayer> nonGroupLayers = new ArrayList<>();
        Set<Integer> readLayerIDs = new HashSet<>();
        Map<Integer,TiledObject> allObjectsByID = new HashMap<>();
        List<PropertyObjectData> propertyObjectsToResolve = new ArrayList<>();
        List<TileObjectToResolve> tileObjectsToResolve = new ArrayList<>();
        OUTER: while (true) {
            next(reader);
            TiledLayer layer = null;
            switch (reader.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "properties":
                            if (properties == null) {
                                properties = readProperties(file, reader, propertyObjectsToResolve);
                            } else {
                                ignoreRedundantTag(reader);
                            }
                            break;
                        case "tileset":
                            if (tileData.gidTiles != null) { //A tile layer has already been read
                                throw new XMLStreamException(describeReaderLocation(reader)
                                        + ": <tileset> tag occurs after the first <layer> tag");
                            }
                            tileData.tilesets.add(readTMXTileset(file, reader, propertyObjectsToResolve));
                            break;
                        case "layer":
                            layer = readLayer(file, reader, nonGroupLayers, readLayerIDs, null,
                                    tileData, propertyObjectsToResolve);
                            break;
                        case "objectgroup":
                            layer = readMapObjectGroup(file, reader, nonGroupLayers, readLayerIDs, null,
                                    allObjectsByID, propertyObjectsToResolve, tileObjectsToResolve);
                            break;
                        case "imagelayer":
                            layer = readImageLayer(file, reader, nonGroupLayers, readLayerIDs, null,
                                    propertyObjectsToResolve);
                            break;
                        case "group":
                            layer = readGroup(file, reader, nonGroupLayers, readLayerIDs, null, tileData,
                                    allObjectsByID, propertyObjectsToResolve, tileObjectsToResolve);
                            break;
                        default:
                            ignoreUnexpectedTag(reader);
                            break;
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    checkEndTagMatch(reader, "map");
                    break OUTER;
                default:
                    ignoreUnexpectedEvent(reader);
                    break;
            }
            if (layer != null) {
                topLevelLayers.add(layer);
            }
        }
        
        List<TiledTileset> mapTilesets = new ArrayList<>(tileData.tilesets.size());
        for (ReferencedTileset mapTileset : tileData.tilesets) {
            mapTilesets.add(mapTileset.tileset);
        }
        
        for (PropertyObjectData data : propertyObjectsToResolve) {
            TiledObject object = allObjectsByID.get(data.objectID);
            if (object == null && data.objectID != 0) {
                throw new XMLStreamException("Object property with name " + data.propertyName
                        + " refers to invalid object ID (" + data.objectID + ")");
            }
            data.properties.put(data.propertyName, object);
        }
        
        if (tileData.gidTiles == null) {
            tileData.initGIDTiles();
        }
        for (TileObjectToResolve objectToResolve : tileObjectsToResolve) {
            objectToResolve.object.tile = tileData.gidTiles.get(objectToResolve.gid);
        }
        
        return new TiledMap(file.getPath(), orientation, renderOrder, width, height,
                tileWidth, tileHeight, hexSideLength, staggerAxis, staggerIndex, backgroundColor,
                mapTilesets, topLevelLayers, nonGroupLayers, properties);
    }
    
    private static TiledTileset readTileset(File file, boolean fileIsTSX, XMLStreamReader reader,
            Map<String,String> attributeValues, List<PropertyObjectData> propertyObjectsToResolve)
            throws XMLStreamException {
        String startLocation = describeReaderLocation(reader);
        
        String name = attributeValues.get("name");
        int tileWidth = parseInt(reader, "tilewidth", attributeValues.get("tilewidth"));
        int tileHeight = parseInt(reader, "tileheight", attributeValues.get("tileheight"));
        int spacing = parseInt(reader, "spacing", attributeValues.get("spacing"), true, 0);
        int margin = parseInt(reader, "margin", attributeValues.get("margin"), true, 0);
        
        int tileCount = parseInt(reader, "tilecount", attributeValues.get("tilecount"));
        if (tileCount < 0) {
            throwInvalidValueException(reader, "tilecount", tileCount, "value must be non-negative");
        }
        int columns = parseInt(reader, "columns", attributeValues.get("columns"));
        if (columns < 0) {
            throwInvalidValueException(reader, "columns", columns, "value must be non-negative");
        }
        TiledTileset.ObjectAlignment alignment = parseEnumValue(TiledTileset.ObjectAlignment.class,
                reader, "objectalignment", attributeValues.get("objectalignment"),
                true, TiledTileset.ObjectAlignment.UNSPECIFIED);
        
        int tileOffsetX = 0;
        int tileOffsetY = 0;
        boolean tileOffsetRead = false;
        
        TiledTileset.GridOrientation gridOrientation = TiledTileset.GridOrientation.ORTHOGONAL;
        int gridWidth = tileWidth;
        int gridHeight = tileHeight;
        boolean gridRead = false;
        
        Map<String,Object> properties = null;
        TiledImage image = null;
        List<TiledTerrainType> terrainTypes = null;
        SortedMap<Integer,TiledTile> idTiles = new TreeMap<>();
        Set<Integer> readTileIDs = new HashSet<>();
        Map<Integer,Integer[]> tileTerrainTypes = new HashMap<>();
        List<TiledWangSet> wangSets = null;
        OUTER: while (true) {
            next(reader);
            switch (reader.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "tileoffset":
                            if (tileOffsetRead) {
                                ignoreRedundantTag(reader);
                            } else {
                                Point tileOffset = readTileOffset(reader);
                                tileOffsetX = tileOffset.x;
                                tileOffsetY = tileOffset.y;
                                tileOffsetRead = true;
                            }
                            break;
                        case "grid":
                            if (gridRead) {
                                ignoreRedundantTag(reader);
                            } else {
                                Grid grid = readGrid(reader);
                                gridOrientation = grid.orientation;
                                gridWidth = grid.width;
                                gridHeight = grid.height;
                                gridRead = true;
                            }
                            break;
                        case "properties":
                            if (properties == null) {
                                properties = readProperties(file, reader, propertyObjectsToResolve);
                            } else {
                                ignoreRedundantTag(reader);
                            }
                            break;
                        case "image":
                            if (image == null) {
                                image = readImage(file, reader);
                            } else {
                                ignoreRedundantTag(reader);
                            }
                            break;
                        case "terraintypes":
                            if (terrainTypes == null) {
                                terrainTypes = readTerrainTypes(reader, idTiles);
                            } else {
                                ignoreRedundantTag(reader);
                            }
                            break;
                        case "tile":
                            readTilesetTile(file, reader, idTiles, readTileIDs, tileTerrainTypes,
                                    propertyObjectsToResolve);
                            break;
                        case "wangsets":
                            if (wangSets == null) {
                                wangSets = readWangSets(reader, idTiles);
                            } else {
                                ignoreRedundantTag(reader);
                            }
                            break;
                        default:
                            ignoreUnexpectedTag(reader);
                            break;
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    checkEndTagMatch(reader, "tileset");
                    break OUTER;
                default:
                    ignoreUnexpectedEvent(reader);
                    break;
            }
        }
        
        if (image == null) {
            //Tileset is (seemingly) an image collection tileset
            for (Map.Entry<Integer,TiledTile> entry : idTiles.entrySet()) {
                int id = entry.getKey();
                TiledTile tile = entry.getValue();
                if (tile.getImage() == null) {
                    throw new XMLStreamException(startLocation + ": <tileset> tag contains no <image> tag,"
                            + " indicating that it's an image collection tileset, but not all of its <tile>"
                            + " tags (e.g. ID " + id + ") contain <image> tags");
                }
            }
        } else {
            //Tileset is (seemingly) a single-image tileset
            if (tileCount > 0) {
                if (columns == 0) {
                    throw new XMLStreamException(startLocation + ": <tileset> tag's tilecount attribute is"
                            + " positive, but its columns attribute is 0");
                }
                if ((tileCount % columns) != 0) {
                    int newTileCount = tileCount - (tileCount % columns) + columns;
                    LOGGER.log(Level.WARNING, "{0}: Tileset''s tile count ({1}) does not divide evenly into"
                            + " its number of columns ({2}); increasing tile count to {3}",
                            new Object[]{describeReaderLocation(reader), tileCount, columns, newTileCount});
                    tileCount = newTileCount;
                }
                for (Map.Entry<Integer,TiledTile> entry : idTiles.entrySet()) {
                    int id = entry.getKey();
                    if (id >= tileCount) {
                        throw new XMLStreamException(startLocation + ": <tileset> tag contains an <image>"
                                + " tag, indicating that it's a single-image tileset, but it contains a"
                                + " <tile> tag with ID " + id + ", which is out of bounds for its tile count"
                                        + " of " + tileCount);
                    }
                    TiledTile tile = entry.getValue();
                    if (tile.getImage() != null) {
                        throw new XMLStreamException(startLocation + ": <tileset> tag contains an <image>"
                                + " tag, indicating that it's a single-image tileset, but it contains a"
                                + " <tile> tag (with ID " + id + ") that also contains an <image> tag");
                    }
                }
                for (int i = 0; i < tileCount; i++) {
                    if (idTiles.get(i) == null) {
                        idTiles.put(i, new TiledTile(i));
                    }
                }
            }
        }
        
        if (terrainTypes != null) {
            for (Map.Entry<Integer,TiledTile> entry : idTiles.entrySet()) {
                int id = entry.getKey();
                if (!tileTerrainTypes.containsKey(id)) {
                    continue; //If the tile's terrain types were never specified, it doesn't have any
                }
                TiledTile tile = entry.getValue();
                for (int cornerIndex = 0; cornerIndex < 4; cornerIndex++) {
                    Integer ttIndex = tileTerrainTypes.get(id)[cornerIndex];
                    if (ttIndex == null) {
                        continue; //No terrain type at this corner
                    }
                    if (ttIndex < 0 || ttIndex >= terrainTypes.size()) {
                        throw new XMLStreamException(describeReaderLocation(reader) + ": <tile> tag with ID "
                                + id + " has invalid terrain type index (" + ttIndex + ")");
                    }
                    TiledTerrainType terrainType = terrainTypes.get(ttIndex);
                    tile.terrainTypes[cornerIndex] = terrainType;
                }
            }
        }
        
        TiledTileset tileset = new TiledTileset((fileIsTSX ? file.getPath() : null), name, tileWidth,
                tileHeight, spacing, margin, idTiles, columns, tileOffsetX, tileOffsetY, alignment,
                gridOrientation, gridWidth, gridHeight, image, terrainTypes, wangSets, properties);
        for (TiledTile tile : idTiles.values()) {
            tile.tileset = tileset;
        }
        return tileset;
    }
    
    private static TiledTile getTile(Map<Integer,TiledTile> idTiles, int id) {
        TiledTile tile = idTiles.get(id);
        if (tile == null) {
            tile = new TiledTile(id);
            idTiles.put(id, tile);
        }
        return tile;
    }
    
    private static ReferencedTileset readTMXTileset(File file, XMLStreamReader reader,
            List<PropertyObjectData> propertyObjectsToResolve) throws XMLStreamException {
        Map<String,String> attributeValues = getAttributeValues(reader, TMX_TILESET_ATTRIBUTES);
        int firstGID = parseInt(reader, "firstgid", attributeValues.get("firstgid"));
        if (firstGID < 0) {
            throwInvalidValueException(reader, "firstgid", firstGID, "value must be non-negative");
        }
        String sourceStr = attributeValues.get("source");
        TiledTileset tileset;
        if (sourceStr == null) {
            for (Map.Entry<String,Boolean> entry : TILESET_ATTRIBUTES.entrySet()) {
                if (entry.getValue() && !attributeValues.containsKey(entry.getKey())) {
                    throwMissingAttributeException(reader, entry.getKey());
                }
            }
            tileset = readTileset(file, false, reader, attributeValues, propertyObjectsToResolve);
        } else {
            File source = parseFile(file, reader, "source", sourceStr);
            tileset = getTileset(source.getPath());
            ensureReference(file, source);
            finishTag(reader);
        }
        return new ReferencedTileset(tileset, firstGID);
    }
    
    private static ReferencedTileset readTXTileset(File file, XMLStreamReader reader)
            throws XMLStreamException {
        Map<String,String> attributeValues = getAttributeValues(reader, TX_TILESET_ATTRIBUTES);
        int firstGID = parseInt(reader, "firstgid", attributeValues.get("firstgid"));
        if (firstGID < 0) {
            throwInvalidValueException(reader, "firstgid", firstGID, "value must be non-negative");
        }
        File source = parseFile(file, reader, "source", attributeValues.get("source"));
        TiledTileset tileset = getTileset(source.getPath());
        ensureReference(file, source);
        finishTag(reader);
        return new ReferencedTileset(tileset, firstGID);
    }
    
    private static Point readTileOffset(XMLStreamReader reader) throws XMLStreamException {
        Map<String,String> attributeValues = getAttributeValues(reader, TILEOFFSET_ATTRIBUTES);
        int x = parseInt(reader, "x", attributeValues.get("x"));
        int y = parseInt(reader, "y", attributeValues.get("y"));
        finishTag(reader);
        return new Point(x, y);
    }
    
    private static class Grid {
        
        private final TiledTileset.GridOrientation orientation;
        private final int width;
        private final int height;
        
        private Grid(TiledTileset.GridOrientation orientation, int width, int height) {
            this.orientation = orientation;
            this.width = width;
            this.height = height;
        }
        
    }
    
    private static Grid readGrid(XMLStreamReader reader) throws XMLStreamException {
        Map<String,String> attributeValues = getAttributeValues(reader, GRID_ATTRIBUTES);
        TiledTileset.GridOrientation orientation = parseEnumValue(TiledTileset.GridOrientation.class,
                reader, "orientation", attributeValues.get("orientation"));
        int width = parseInt(reader, "width", attributeValues.get("width"));
        int height = parseInt(reader, "height", attributeValues.get("height"));
        finishTag(reader);
        return new Grid(orientation, width, height);
    }
    
    private static TiledImage readImage(File file, XMLStreamReader reader) throws XMLStreamException {
        Map<String,String> attributeValues = getAttributeValues(reader, IMAGE_ATTRIBUTES);
        
        Color trans = parseColor(reader, "trans", attributeValues.get("trans"), true, null);
        int width = parseInt(reader, "width", attributeValues.get("width"), true, -1);
        int height = parseInt(reader, "height", attributeValues.get("height"), true, -1);
        
        //Image image;
        String source;
        String format = attributeValues.get("format");
        String sourceStr = attributeValues.get("source");
        if (sourceStr == null) {
            /*
            if (format == null) {
                throwMissingAttributeException(reader, "format");
                return null;
            }
            image = readEmbeddedImage(reader, format);
            source = null;
            */
            throwMissingAttributeException(reader, "source");
            return null;
        } else {
            if (format != null) {
                giveUnexpectedAttributeWarning(reader, "format");
            }
            //image = null;
            source = readExternalImage(file, reader, sourceStr);
        }
        //return new TiledImage(image, source, trans, width, height);
        return new TiledImage(source, trans, width, height);
    }
    
    private static String readExternalImage(File file, XMLStreamReader reader, String sourceStr)
            throws XMLStreamException {
        String source = parseFile(file, reader, "source", sourceStr).getPath();
        finishTag(reader);
        return source;
    }
    
    private static List<TiledTerrainType> readTerrainTypes(XMLStreamReader reader,
            Map<Integer,TiledTile> idTiles) throws XMLStreamException {
        getAttributeValues(reader, Collections.emptyMap());
        List<TiledTerrainType> terrainTypes = new ArrayList<>();
        OUTER: while (true) {
            next(reader);
            switch (reader.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "terrain":
                            readTerrain(reader, idTiles, terrainTypes);
                            break;
                        default:
                            ignoreUnexpectedTag(reader);
                            break;
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    checkEndTagMatch(reader, "terraintypes");
                    break OUTER;
                default:
                    ignoreUnexpectedEvent(reader);
                    break;
            }
        }
        return terrainTypes;
    }
    
    private static void readTerrain(XMLStreamReader reader, Map<Integer,TiledTile> idTiles,
            List<TiledTerrainType> terrainTypes) throws XMLStreamException {
        Map<String,String> attributeValues = getAttributeValues(reader, TERRAIN_ATTRIBUTES);
        
        String name = attributeValues.get("name");
        
        String tileIDStr = attributeValues.get("tile");
        int tileID = parseInt(reader, "tile", tileIDStr);
        if (tileID < 0) {
            throwInvalidValueException(reader, "tile", tileIDStr, "value must be non-negative");
        }
        TiledTile tile = getTile(idTiles, tileID);
        
        finishTag(reader);
        terrainTypes.add(new TiledTerrainType(name, tile));
    }
    
    private static class AnimationFrame {
        
        private final TiledTile frame;
        private final int duration;
        
        private AnimationFrame(TiledTile frame, int duration) {
            this.frame = frame;
            this.duration = duration;
        }
        
    }
    
    private static void readTilesetTile(File file, XMLStreamReader reader, Map<Integer,TiledTile> idTiles,
            Set<Integer> readTileIDs, Map<Integer,Integer[]> tileTerrainTypes,
            List<PropertyObjectData> propertyObjectsToResolve) throws XMLStreamException {
        Map<String,String> attributeValues = getAttributeValues(reader, TILESET_TILE_ATTRIBUTES);
        
        String idStr = attributeValues.get("id");
        int id = parseInt(reader, "id", idStr);
        if (id < 0) {
            throwInvalidValueException(reader, "id", idStr, "value must be non-negative");
        }
        if (!readTileIDs.add(id)) {
            ignoreRedundantTag(reader);
            return;
        }
        TiledTile tile = getTile(idTiles, id);
        
        tile.type = attributeValues.get("type");
        
        String terrain = attributeValues.get("terrain");
        Integer[] terrainTypes = new Integer[4];
        tileTerrainTypes.put(id, terrainTypes);
        if (terrain != null) {
            int cornerIndex = 0;
            int startOfSubstring = 0;
            for (int i = 0; i <= terrain.length(); i++) {
                if (i == terrain.length() || terrain.charAt(i) == ',') {
                    if (cornerIndex == 4) {
                        throw new XMLStreamException(describeReaderLocation(reader)
                                + ": Value of <tile> tag's terrain attribute (" + terrain
                                + ") contains more than 4 comma-separated values");
                    }
                    try {
                        terrainTypes[cornerIndex] = Integer.parseInt(
                                terrain.substring(startOfSubstring, i).trim());
                    } catch (NumberFormatException e) {
                        throw new XMLStreamException(describeReaderLocation(reader)
                                + ": Value of <tile> tag's terrain attribute (" + terrain
                                + ") contains a non-integer value");
                    }
                    cornerIndex++;
                    startOfSubstring = i + 1;
                }
            }
            if (cornerIndex != 4) {
                throw new XMLStreamException(describeReaderLocation(reader)
                        + ": Value of <tile> tag's terrain attribute (" + terrain
                        + ") contains fewer than 4 comma-separated values");
            }
        }
        
        String probabilityStr = attributeValues.get("probability");
        if (probabilityStr != null) {
            tile.probability = parseFloat(reader, "probability", probabilityStr);
        }
        
        TiledImage image = null;
        List<TiledObject> collisionObjects = null;
        List<TiledTile> frames = null;
        List<Integer> frameDurations = null;
        Map<String,Object> properties = null;
        OUTER: while (true) {
            next(reader);
            switch (reader.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "properties":
                            if (properties == null) {
                                properties = readProperties(file, reader, propertyObjectsToResolve);
                            } else {
                                ignoreRedundantTag(reader);
                            }
                            break;
                        case "image":
                            if (image == null) {
                                image = readImage(file, reader);
                            } else {
                                ignoreRedundantTag(reader);
                            }
                            break;
                        case "objectgroup":
                            if (collisionObjects == null) {
                                collisionObjects = readTileObjectGroup(file, reader);
                            } else {
                                ignoreRedundantTag(reader);
                            }
                            break;
                        case "animation":
                            if (frames == null) {
                                List<AnimationFrame> animation = readAnimation(reader, idTiles);
                                frames = new ArrayList<>();
                                frameDurations = new ArrayList<>();
                                for (AnimationFrame animationFrame : animation) {
                                    frames.add(animationFrame.frame);
                                    frameDurations.add(animationFrame.duration);
                                }
                            } else {
                                ignoreRedundantTag(reader);
                            }
                            break;
                        default:
                            ignoreUnexpectedTag(reader);
                            break;
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    checkEndTagMatch(reader, "tile");
                    break OUTER;
                default:
                    ignoreUnexpectedEvent(reader);
                    break;
            }
        }
        tile.setInnerTagInfo(image, collisionObjects, frames, frameDurations, properties);
    }
    
    private static List<AnimationFrame> readAnimation(XMLStreamReader reader,
            Map<Integer,TiledTile> idTiles) throws XMLStreamException {
        getAttributeValues(reader, Collections.emptyMap());
        List<AnimationFrame> animation = new ArrayList<>();
        OUTER: while (true) {
            next(reader);
            switch (reader.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "frame":
                            animation.add(readFrame(reader, idTiles));
                            break;
                        default:
                            ignoreUnexpectedTag(reader);
                            break;
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    checkEndTagMatch(reader, "animation");
                    break OUTER;
                default:
                    ignoreUnexpectedEvent(reader);
                    break;
            }
        }
        if (animation.isEmpty()) {
            throw new XMLStreamException(describeReaderLocation(reader)
                    + ": <animation> tag contains no <frame> tags");
        }
        return animation;
    }
    
    private static AnimationFrame readFrame(XMLStreamReader reader, Map<Integer,TiledTile> idTiles)
            throws XMLStreamException {
        Map<String,String> attributeValues = getAttributeValues(reader, FRAME_ATTRIBUTES);
        String tileIDStr = attributeValues.get("tileid");
        int tileID = parseInt(reader, "tileid", tileIDStr);
        if (tileID < 0) {
            throwInvalidValueException(reader, "tileid", tileIDStr, "value must be non-negative");
        }
        int duration = parseInt(reader, "duration", attributeValues.get("duration"));
        finishTag(reader);
        return new AnimationFrame(getTile(idTiles, tileID), duration);
    }
    
    private static List<TiledWangSet> readWangSets(XMLStreamReader reader, Map<Integer,TiledTile> idTiles)
            throws XMLStreamException {
        getAttributeValues(reader, Collections.emptyMap());
        List<TiledWangSet> wangSets = new ArrayList<>();
        OUTER: while (true) {
            next(reader);
            switch (reader.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "wangset":
                            wangSets.add(readWangSet(reader, idTiles));
                            break;
                        default:
                            ignoreUnexpectedTag(reader);
                            break;
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    checkEndTagMatch(reader, "wangsets");
                    break OUTER;
                default:
                    ignoreUnexpectedEvent(reader);
                    break;
            }
        }
        return wangSets;
    }
    
    private static TiledWangSet readWangSet(XMLStreamReader reader, Map<Integer,TiledTile> idTiles)
            throws XMLStreamException {
        Map<String,String> attributeValues = getAttributeValues(reader, WANGSET_ATTRIBUTES);
        
        String name = attributeValues.get("name");
        
        int tileID = parseInt(reader, "tile", attributeValues.get("tile"));
        TiledTile tile = (tileID < 0 ? null : getTile(idTiles, tileID));
        
        int bitsPerColor = 4;
        int maxColorsPerType = (1 << bitsPerColor) - 1;
        List<TiledWangColor> cornerColors = new ArrayList<>();
        List<TiledWangColor> edgeColors = new ArrayList<>();
        Map<TiledTile,String> wangTileData = new HashMap<>();
        OUTER: while (true) {
            next(reader);
            switch (reader.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "wangcornercolor":
                            if (cornerColors.size() == maxColorsPerType) {
                                throw new XMLStreamException(describeReaderLocation(reader)
                                        + ": <wangset> tag contains more than "
                                        + maxColorsPerType + " <wangcornercolor> tags");
                            }
                            cornerColors.add(readWangColor(reader, idTiles));
                            break;
                        case "wangedgecolor":
                            if (edgeColors.size() == maxColorsPerType) {
                                throw new XMLStreamException(describeReaderLocation(reader)
                                        + ": <wangset> tag contains more than "
                                        + maxColorsPerType + " <wangedgecolor> tags");
                            }
                            edgeColors.add(readWangColor(reader, idTiles));
                            break;
                        case "wangtile":
                            readWangTile(reader, idTiles, wangTileData);
                            break;
                        default:
                            ignoreUnexpectedTag(reader);
                            break;
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    checkEndTagMatch(reader, "wangset");
                    break OUTER;
                default:
                    ignoreUnexpectedEvent(reader);
                    break;
            }
        }
        
        Map<TiledTile,TiledWangTile> wangTiles = new HashMap<>();
        for (Map.Entry<TiledTile,String> entry : wangTileData.entrySet()) {
            TiledTile entryTile = entry.getKey();
            String wangID = entry.getValue();
            if (!(wangID.length() == 10) && (wangID.substring(0, 2).equals("0x"))) {
                throw new XMLStreamException(describeReaderLocation(reader)
                        + ": <wangtile> tag for tile ID " + entryTile.getID()
                        + " has invalid wangid attribute");
            }
            TiledWangColor[] wangTileColors = new TiledWangColor[8];
            for (int i = 0; i < wangTileColors.length; i++) {
                int colorIndex = parseHexDigit(wangID.charAt(9 - i));
                if (colorIndex == -1) {
                    throw new XMLStreamException(describeReaderLocation(reader)
                            + ": <wangtile> tag for tile ID " + entryTile.getID()
                            + " has invalid wangid attribute");
                }
                if (colorIndex == 0) {
                    continue;
                }
                List<TiledWangColor> colorList = ((i % 2) == 0 ? edgeColors : cornerColors);
                wangTileColors[i] = colorList.get(colorIndex - 1);
            }
            wangTiles.put(entryTile, new TiledWangTile(entryTile, wangTileColors));
        }
        return new TiledWangSet(name, tile, cornerColors, edgeColors, wangTiles);
    }
    
    private static TiledWangColor readWangColor(XMLStreamReader reader, Map<Integer,TiledTile> idTiles)
            throws XMLStreamException {
        Map<String,String> attributeValues = getAttributeValues(reader, WANG_COLOR_ATTRIBUTES);
        
        String name = attributeValues.get("name");
        Color color = parseColor(reader, "color", attributeValues.get("color"));
        
        int tileID = parseInt(reader, "tile", attributeValues.get("tile"));
        TiledTile tile = (tileID < 0 ? null : getTile(idTiles, tileID));
        
        float probability = parseFloat(reader, "probability", attributeValues.get("probability"));
        
        finishTag(reader);
        return new TiledWangColor(name, color, tile, probability);
    }
    
    private static void readWangTile(XMLStreamReader reader, Map<Integer,TiledTile> idTiles,
            Map<TiledTile,String> wangTileData) throws XMLStreamException {
        Map<String,String> attributeValues = getAttributeValues(reader, WANGTILE_ATTRIBUTES);
        
        String tileIDStr = attributeValues.get("tileid");
        int tileID = parseInt(reader, "tileid", tileIDStr);
        if (tileID < 0) {
            throwInvalidValueException(reader, "tileid", tileIDStr, "value must be non-negative");
        }
        TiledTile tile = getTile(idTiles, tileID);
        
        if (wangTileData.get(tile) == null) {
            finishTag(reader);
            wangTileData.put(tile, attributeValues.get("wangid"));
        } else {
            ignoreRedundantTag(reader);
        }
    }
    
    private static TiledTileLayer readLayer(File file, XMLStreamReader reader,
            List<TiledLayer> nonGroupLayers, Set<Integer> readLayerIDs, TiledGroupLayer parent,
            MapTileData tileData, List<PropertyObjectData> propertyObjectsToResolve)
            throws XMLStreamException {
        if (tileData.gidTiles == null) {
            tileData.initGIDTiles();
        }
        Map<String,String> attributeValues = getAttributeValues(reader, LAYER_ATTRIBUTES);
        
        int id = parseInt(reader, "id", attributeValues.get("id"));
        if (!readLayerIDs.add(id)) {
            ignoreRedundantTag(reader);
            return null;
        }
        
        String name = attributeValues.get("name");
        int x = parseInt(reader, "x", attributeValues.get("x"), true, 0);
        int y = parseInt(reader, "y", attributeValues.get("y"), true, 0);
        int width = parseInt(reader, "width", attributeValues.get("width"));
        int height = parseInt(reader, "height", attributeValues.get("height"));
        float opacity = parseFloat(reader, "opacity", attributeValues.get("opacity"), true, 1);
        boolean visible = parseBooleanFromInt(reader, "visible", attributeValues.get("visible"), true, true);
        Color tintColor = parseColor(reader, "tintcolor", attributeValues.get("tintcolor"),
                true, DEFAULT_TINT_COLOR);
        float offsetX = parseFloat(reader, "offsetx", attributeValues.get("offsetx"), true, 0);
        float offsetY = parseFloat(reader, "offsety", attributeValues.get("offsety"), true, 0);
        
        Map<String,Object> properties = null;
        int x1 = 0;
        int y1 = 0;
        int x2 = -1;
        int y2 = -1;
        Map<Point,TiledTile> tiles = null;
        Map<Point,Integer> flags = null;
        OUTER: while (true) {
            next(reader);
            switch (reader.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "properties":
                            if (properties == null) {
                                properties = readProperties(file, reader, propertyObjectsToResolve);
                            } else {
                                ignoreRedundantTag(reader);
                            }
                            break;
                        case "data":
                            if (tiles == null) {
                                Map<Point,Integer> data = readTileLayerData(reader, x, y, width, height);
                                tiles = new HashMap<>();
                                flags = new HashMap<>();
                                for (Map.Entry<Point,Integer> entry : data.entrySet()) {
                                    Point point = entry.getKey();
                                    if (x1 == 0 && x2 == -1) {
                                        x1 = point.x;
                                        y1 = point.y;
                                        x2 = point.x;
                                        y2 = point.y;
                                    } else {
                                        x1 = Math.min(x1, point.x);
                                        y1 = Math.min(y1, point.y);
                                        x2 = Math.max(x2, point.x);
                                        y2 = Math.max(y2, point.y);
                                    }
                                    
                                    int value = entry.getValue();
                                    
                                    int gid = value & ~FL_TILE_ALL;
                                    TiledTile tile = tileData.gidTiles.get(gid);
                                    if (tile != null) {
                                        tiles.put(point, tile);
                                    }
                                    
                                    int tileFlags = 0;
                                    if ((value & FL_TILE_FLIPX) != 0) {
                                        tileFlags += TiledTileLayer.FL_FLIPX;
                                    }
                                    if ((value & FL_TILE_FLIPY) != 0) {
                                        tileFlags += TiledTileLayer.FL_FLIPY;
                                    }
                                    if ((value & FL_TILE_FLIPD) != 0) {
                                        tileFlags += TiledTileLayer.FL_FLIPD;
                                    }
                                    if (tileFlags != 0) {
                                        flags.put(point, tileFlags);
                                    }
                                }
                            } else {
                                ignoreRedundantTag(reader);
                            }
                            break;
                        default:
                            ignoreUnexpectedTag(reader);
                            break;
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    checkEndTagMatch(reader, "layer");
                    break OUTER;
                default:
                    ignoreUnexpectedEvent(reader);
                    break;
            }
        }
        
        int layerArea = (x2 - x1 + 1)*(y2 - y1 + 1);
        TiledTileLayer layer;
        if (tiles != null && tiles.size() < ((double)layerArea)/4) {
            //Layer's tiles are sparse; represent it with a HashTileLayer
            layer = new HashTileLayer(name, parent, opacity, visible, tintColor, offsetX, offsetY,
                    x1, y1, x2, y2, tiles, flags);
        } else {
            //Layer's tiles are dense; represent it with an ArrayTileLayer
            layer = new ArrayTileLayer(name, parent, opacity, visible, tintColor, offsetX, offsetY,
                    x1, y1, x2, y2, tiles, flags);
        }
        layer.setProperties(properties);
        nonGroupLayers.add(layer);
        return layer;
    }
    
    private static int[][] decodeTileData(XMLStreamReader reader, String dataStr,
            String encoding, String compression, int width, int height) throws XMLStreamException {
        int[][] data = new int[width][height];
        if (encoding.equals("base64")) {
            int expectedDataSize = 4 * width * height;
            byte[] bytes = BASE_64_DECODER.decode(dataStr);
            int dataSize = bytes.length;
            if (compression != null) {
                switch (compression) {
                    case "gzip":
                        try {
                            GZIPInputStream stream = new GZIPInputStream(new ByteArrayInputStream(bytes));
                            bytes = new byte[expectedDataSize];
                            dataSize = stream.read(bytes, 0, expectedDataSize);
                        } catch (IOException e) {
                            throw new XMLStreamException(describeReaderLocation(reader)
                                    + ": Failed to decompress GZIP-compressed embedded tile data");
                        }
                        break;
                    case "zlib":
                        try {
                            Inflater inflater = new Inflater();
                            inflater.setInput(bytes, 0, bytes.length);
                            bytes = new byte[expectedDataSize];
                            dataSize = inflater.inflate(bytes);
                            inflater.end();
                        } catch (DataFormatException e) {
                            throw new XMLStreamException(describeReaderLocation(reader)
                                    + ": Failed to decompress ZLIB-compressed embedded tile data");
                        }
                        break;
                }
            }
            if (dataSize != expectedDataSize) {
                throw new XMLStreamException(describeReaderLocation(reader)
                        + ": Embedded tile data is the wrong size (expected 4 * " + width + " * " + height
                        + " bytes, got " + dataSize + " bytes)");
            }
            int k = 0;
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    data[i][j] = Byte.toUnsignedInt(bytes[k])
                            + (Byte.toUnsignedInt(bytes[k + 1]) << 8)
                            + (Byte.toUnsignedInt(bytes[k + 2]) << 16)
                            + (Byte.toUnsignedInt(bytes[k + 3]) << 24);
                    k += 4;
                }
            }
        } else {
            //encoding == csv
            String[] values = dataStr.split(",");
            if (values.length != width * height) {
                throw new XMLStreamException(describeReaderLocation(reader)
                        + ": Embedded tile data is the wrong size (expected " + width + " * " + height
                        + " values, got " + values.length + " values)");
            }
            int k = 0;
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    try {
                        data[i][j] = Integer.parseUnsignedInt(values[k].trim());
                    } catch (NumberFormatException e) {
                        throw new XMLStreamException(describeReaderLocation(reader)
                                + ": CSV-encoded embedded tile data contains a value (" + values[k]
                                + ") that could not be parsed as an unsigned integer");
                    }
                    k++;
                }
            }
            
        }
        return data;
    }
    
    private static Map<Point,Integer> readTileLayerData(
            XMLStreamReader reader, int x, int y, int width, int height) throws XMLStreamException {
        Map<String,String> attributeValues = getAttributeValues(reader, DATA_ATTRIBUTES);
        
        String encoding = attributeValues.get("encoding");
        if (!(encoding == null || encoding.equals("base64") || encoding.equals("csv"))) {
            throwInvalidValueException(reader, "encoding", encoding,
                    "value must be 'base64' or 'csv', or attribute must be absent");
        }
        
        String compression = attributeValues.get("compression");
        if (!(compression == null || compression.equals("gzip") || compression.equals("zlib"))) {
            throwInvalidValueException(reader, "compression", compression,
                    "value must be 'gzip' or 'zlib', or attribute must be absent");
        } else if (compression != null && !"base64".equals(encoding)) {
            giveUnexpectedAttributeWarning(reader, "compression");
            compression = null;
        }
        
        boolean dataInTileTags = (encoding == null);
        int i = 0;
        int j = 0;
        
        int[][] decodedData = null;
        Map<Point,Integer> data = new HashMap<>();
        OUTER: while (true) {
            next(reader);
            switch (reader.getEventType()) {
                case XMLStreamConstants.CHARACTERS:
                case XMLStreamConstants.CDATA:
                    if (!dataInTileTags && decodedData == null) {
                        String dataStr = reader.getText().trim();
                        decodedData = decodeTileData(reader, dataStr, encoding, compression, width, height);
                    } else {
                        ignoreUnexpectedEvent(reader);
                    }
                    break;
                case XMLStreamConstants.START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "chunk":
                            Chunk chunk = readChunk(reader, encoding, compression);
                            for (int k = 0; k < chunk.width; k++) {
                                int[] column = chunk.data[k];
                                for (int m = 0; m < chunk.height; m++) {
                                    int value = column[m];
                                    if (value != 0) {
                                        data.put(new Point(chunk.x + k, chunk.y + m), value);
                                    }
                                }
                            }
                            break;
                        case "tile":
                            if (dataInTileTags) {
                                if (j == height) {
                                    throw new XMLStreamException(describeReaderLocation(reader)
                                            + ": <data> tag contains too many <tile> tags (expected: "
                                            + width + " * " + height + ")");
                                }
                                data.put(new Point(x + i, y + j), readDataTile(reader));
                                i++;
                                if (i == width) {
                                    i = 0;
                                    j++;
                                }
                            } else {
                                ignoreUnexpectedTag(reader);
                            }
                            break;
                        default:
                            ignoreUnexpectedTag(reader);
                            break;
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    checkEndTagMatch(reader, "data");
                    break OUTER;
                default:
                    ignoreUnexpectedEvent(reader);
                    break;
            }
        }
        if (dataInTileTags && !(i == 0 && j == height)) {
            throw new XMLStreamException(describeReaderLocation(reader)
                    + ": <data> tag contains too few <tile> tags (expected: "
                    + width + " * " + height + ")");
        }
        
        if (decodedData != null) {
            for (int k = 0; k < width; k++) {
                int[] column = decodedData[k];
                for (int m = 0; m < height; m++) {
                    int value = column[m];
                    if (value != 0) {
                        data.put(new Point(x + k, y + m), value);
                    }
                }
            }
        }
        
        return data;
    }
    
    private static class Chunk {
        
        private final int x, y, width, height;
        private final int[][] data;
        
        private Chunk(int x, int y, int width, int height, int[][] data) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.data = data;
        }
        
    }
    
    private static Chunk readChunk(XMLStreamReader reader, String encoding, String compression)
            throws XMLStreamException {
        Map<String,String> attributeValues = getAttributeValues(reader, CHUNK_ATTRIBUTES);
        
        int x = parseInt(reader, "x", attributeValues.get("x"));
        int y = parseInt(reader, "y", attributeValues.get("y"));
        
        int width = parseInt(reader, "width", attributeValues.get("width"));
        if (width <= 0) {
            throwInvalidValueException(reader, "width", width, "value must be positive");
        }
        int height = parseInt(reader, "height", attributeValues.get("height"));
        if (height <= 0) {
            throwInvalidValueException(reader, "height", height, "value must be positive");
        }
        
        int[][] tileTagData = (encoding == null ? new int[width][height] : null);
        int i = 0;
        int j = 0;
        
        int[][] decodedData = null;
        
        OUTER: while (true) {
            next(reader);
            switch (reader.getEventType()) {
                case XMLStreamConstants.CHARACTERS:
                case XMLStreamConstants.CDATA:
                    if (tileTagData == null && decodedData == null) {
                        String dataStr = reader.getText().trim();
                        decodedData = decodeTileData(reader, dataStr, encoding, compression, width, height);
                    } else {
                        ignoreUnexpectedEvent(reader);
                    }
                    break;
                case XMLStreamConstants.START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "tile":
                            if (tileTagData == null) {
                                ignoreUnexpectedTag(reader);
                            } else {
                                if (j == height) {
                                    throw new XMLStreamException(describeReaderLocation(reader)
                                            + ": <chunk> tag contains too many <tile> tags (expected: "
                                            + width + " * " + height + ")");
                                }
                                tileTagData[i][j] = readDataTile(reader);
                                i++;
                                if (i == width) {
                                    i = 0;
                                    j++;
                                }
                            }
                            break;
                        default:
                            ignoreUnexpectedTag(reader);
                            break;
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    checkEndTagMatch(reader, "chunk");
                    break OUTER;
                default:
                    ignoreUnexpectedEvent(reader);
                    break;
            }
        }
        
        if (tileTagData == null) {
            if (decodedData == null) {
                throw new XMLStreamException(describeReaderLocation(reader)
                        + ": <chunk> tag contains no data");
            }
            return new Chunk(x, y, width, height, decodedData);
        } else {
            if (!(i == 0 && j == height)) {
                throw new XMLStreamException(describeReaderLocation(reader)
                    + ": <chunk> tag contains too few <tile> tags (expected: "
                    + width + " * " + height + ")");
            }
            return new Chunk(x, y, width, height, tileTagData);
        }
    }
    
    private static int readDataTile(XMLStreamReader reader) throws XMLStreamException {
        Map<String,String> attributeValues = getAttributeValues(reader, DATA_TILE_ATTRIBUTES);
        int gid = parseInt(reader, "gid", attributeValues.get("gid"), true, 0);
        finishTag(reader);
        return gid;
    }
    
    private static TiledObjectLayer readMapObjectGroup(File file, XMLStreamReader reader,
            List<TiledLayer> nonGroupLayers, Set<Integer> readLayerIDs, TiledGroupLayer parent,
            Map<Integer,TiledObject> allObjectsByID, List<PropertyObjectData> propertyObjectsToResolve,
            List<TileObjectToResolve> tileObjectsToResolve) throws XMLStreamException {
        Map<String,String> attributeValues = getAttributeValues(reader, MAP_OBJECTGROUP_ATTRIBUTES);
        
        int id = parseInt(reader, "id", attributeValues.get("id"));
        if (!readLayerIDs.add(id)) {
            ignoreRedundantTag(reader);
            return null;
        }
        
        String name = attributeValues.get("name");
        Color color = parseColor(reader, "color", attributeValues.get("color"),
                true, new Color(160, 160, 164));
        int x = parseInt(reader, "x", attributeValues.get("x"), true, 0);
        int y = parseInt(reader, "y", attributeValues.get("y"), true, 0);
        int width = parseInt(reader, "width", attributeValues.get("width"), true, 0);
        int height = parseInt(reader, "height", attributeValues.get("height"), true, 0);
        float opacity = parseFloat(reader, "opacity", attributeValues.get("opacity"), true, 1);
        boolean visible = parseBooleanFromInt(reader, "visible", attributeValues.get("visible"), true, true);
        Color tintColor = parseColor(reader, "tintcolor", attributeValues.get("tintcolor"),
                true, DEFAULT_TINT_COLOR);
        float offsetX = parseFloat(reader, "offsetx", attributeValues.get("offsetx"), true, 0);
        float offsetY = parseFloat(reader, "offsety", attributeValues.get("offsety"), true, 0);
        
        Map<String,Object> properties = null;
        List<TiledObject> objects = new ArrayList<>();
        OUTER: while (true) {
            next(reader);
            switch (reader.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "properties":
                            if (properties == null) {
                                properties = readProperties(file, reader, propertyObjectsToResolve);
                            } else {
                                ignoreRedundantTag(reader);
                            }
                            break;
                        case "object":
                            readObject(file, reader, objects,
                                    allObjectsByID, propertyObjectsToResolve, tileObjectsToResolve);
                            break;
                        default:
                            ignoreUnexpectedTag(reader);
                            break;
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    checkEndTagMatch(reader, "objectgroup");
                    break OUTER;
                default:
                    ignoreUnexpectedEvent(reader);
                    break;
            }
        }
        
        TiledObjectLayer layer = new TiledObjectLayer(
                name, parent, opacity, visible, tintColor, offsetX, offsetY, color, objects);
        layer.setProperties(properties);
        nonGroupLayers.add(layer);
        return layer;
    }
    
    private static List<TiledObject> readTileObjectGroup(File file, XMLStreamReader reader)
            throws XMLStreamException {
        getAttributeValues(reader, TILE_OBJECTGROUP_ATTRIBUTES);
        
        List<TiledObject> objects = new ArrayList<>();
        Map<Integer,TiledObject> allObjectsByID = new HashMap<>();
        List<PropertyObjectData> propertyObjectsToResolve = new ArrayList<>();
        OUTER: while (true) {
            next(reader);
            switch (reader.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "object":
                            readObject(file, reader,
                                    objects, allObjectsByID, propertyObjectsToResolve, null);
                            break;
                        default:
                            ignoreUnexpectedTag(reader);
                            break;
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    checkEndTagMatch(reader, "objectgroup");
                    break OUTER;
                default:
                    ignoreUnexpectedEvent(reader);
                    break;
            }
        }
        
        for (PropertyObjectData data : propertyObjectsToResolve) {
            TiledObject object = allObjectsByID.get(data.objectID);
            if (object == null && data.objectID != 0) {
                throw new XMLStreamException("Object property with name " + data.propertyName
                        + " refers to invalid object ID (" + data.objectID + ")");
            }
            data.properties.put(data.propertyName, object);
        }
        
        return objects;
    }
    
    private static class ObjectData {
        
        private String name, type;
        private float width, height, rotation;
        private int unresolvedGID;
        private TiledTile tile;
        private int tileFlags;
        private boolean visible;
        private TiledObject.Shape shape;
        private List<Point2D> points;
        private TiledText text;
        private Map<String,Object> properties;
        
        private ObjectData() {
            name = "";
            type = "";
            width = 0;
            height = 0;
            rotation = 0;
            unresolvedGID = 0;
            tile = null;
            tileFlags = 0;
            visible = true;
            shape = TiledObject.Shape.RECTANGLE;
            points = null;
            text = null;
            properties = new HashMap<>();
        }
        
        private ObjectData(TiledObjectTemplate template) {
            name = template.getName();
            type = template.getType();
            width = template.getWidth();
            height = template.getHeight();
            rotation = template.getRotation();
            unresolvedGID = 0;
            tile = template.getTile();
            tileFlags = 0;
            if (template.getTileXFlip()) {
                tileFlags |= TiledTileLayer.FL_FLIPX;
            }
            if (template.getTileYFlip()) {
                tileFlags |= TiledTileLayer.FL_FLIPY;
            }
            if (template.getTileDFlip()) {
                tileFlags |= TiledTileLayer.FL_FLIPD;
            }
            visible = template.getVisible();
            shape = template.getShape();
            points = template.getPoints();
            text = template.getText();
            properties = new HashMap<>(template.getProperties());
        }
        
    }
    
    private static void updateObjectData(XMLStreamReader reader, ObjectData data,
            Map<String,String> attributeValues) throws XMLStreamException {
        String name = attributeValues.get("name");
        if (name != null) {
            data.name = name;
        }
        String type = attributeValues.get("name");
        if (type != null) {
            data.type = type;
        }
        String widthStr = attributeValues.get("width");
        if (widthStr != null) {
            data.width = parseFloat(reader, "width", widthStr);
        }
        String heightStr = attributeValues.get("height");
        if (heightStr != null) {
            data.height = parseFloat(reader, "height", heightStr);
        }
        String rotationStr = attributeValues.get("rotation");
        if (rotationStr != null) {
            data.rotation = parseFloat(reader, "rotation", rotationStr);
        }
        String gidStr = attributeValues.get("gid");
        if (gidStr != null) {
            int rawGID;
            try {
                rawGID = Integer.parseUnsignedInt(gidStr);
            } catch (NumberFormatException e) {
                throw new XMLStreamException(describeReaderLocation(reader)
                        + ": <object> tag's gid attribute could not be parsed as an unsigned integer");
            }
            data.unresolvedGID = rawGID & ~FL_TILE_ALL;
            if ((rawGID & FL_TILE_FLIPX) != 0) {
                data.tileFlags |= TiledTileLayer.FL_FLIPX;
            }
            if ((rawGID & FL_TILE_FLIPY) != 0) {
                data.tileFlags |= TiledTileLayer.FL_FLIPY;
            }
            if ((rawGID & FL_TILE_FLIPD) != 0) {
                data.tileFlags |= TiledTileLayer.FL_FLIPD;
            }
            data.tile = null;
        }
        String visibleStr = attributeValues.get("visible");
        if (visibleStr != null) {
            data.visible = parseBooleanFromInt(reader, "visible", visibleStr);
        }
    }
    
    private static void readShape(XMLStreamReader reader, ObjectData data) throws XMLStreamException {
        String shapeName = reader.getLocalName();
        data.shape = TiledObject.Shape.valueOf(shapeName.toUpperCase());
        Map<String,String> attributeValues;
        switch (data.shape) {
            case ELLIPSE:
            case POINT:
                data.points = null;
                data.text = null;
                finishTag(reader);
                break;
            case POLYGON:
            case POLYLINE:
                attributeValues = getAttributeValues(reader, Collections.singletonMap("points", true));
                String pointsStr = attributeValues.get("points");
                data.points = new ArrayList<>();
                String[] coordPairs = pointsStr.split(" ");
                for (String coordPair : coordPairs) {
                    String[] coords = coordPair.split(",");
                    boolean pairIsInvalid = false;
                    float x = 0;
                    float y = 0;
                    if (coords.length == 2) {
                        try {
                            x = Float.parseFloat(coords[0]);
                            y = Float.parseFloat(coords[1]);
                        } catch (NumberFormatException e) {
                            pairIsInvalid = true;
                        }
                    } else {
                        pairIsInvalid = true;
                    }
                    if (pairIsInvalid) {
                        throw new XMLStreamException(describeReaderLocation(reader)
                                + ": Invalid coordinate pair (" + coordPair + ") in points attribute");
                    } else {
                        data.points.add(new Point2D.Float(x, y));
                    }
                }
                data.text = null;
                finishTag(reader);
                break;
            case TEXT:
                data.points = null;
                attributeValues = getAttributeValues(reader, TEXT_ATTRIBUTES);
                String fontFamily = attributeValues.get("fontfamily");
                if (fontFamily == null) {
                    fontFamily = "sans-serif";
                }
                int pixelSize = parseInt(reader, "pixelsize", attributeValues.get("pixelsize"), true, 16);
                boolean wrap = parseBooleanFromInt(reader, "wrap", attributeValues.get("wrap"), true, false);
                Color color = parseColor(reader, "color", attributeValues.get("color"), true, Color.BLACK);
                boolean bold = parseBooleanFromInt(reader, "bold", attributeValues.get("bold"), true, false);
                boolean italic = parseBooleanFromInt(reader,
                        "italic", attributeValues.get("italic"), true, false);
                boolean underline = parseBooleanFromInt(reader,
                        "underline", attributeValues.get("underline"), true, false);
                boolean strikeout = parseBooleanFromInt(reader,
                        "strikeout", attributeValues.get("strikeout"), true, false);
                boolean kerning = parseBooleanFromInt(reader,
                        "kerning", attributeValues.get("kerning"), true, false);
                TiledText.HAlign hAlign = parseEnumValue(TiledText.HAlign.class, reader,
                        "halign", attributeValues.get("halign"), true, TiledText.HAlign.LEFT);
                TiledText.VAlign vAlign = parseEnumValue(TiledText.VAlign.class, reader,
                        "valign", attributeValues.get("valign"), true, TiledText.VAlign.TOP);
                
                String content = null;
                OUTER: while (true) {
                    next(reader);
                    switch (reader.getEventType()) {
                        case XMLStreamConstants.CHARACTERS:
                        case XMLStreamConstants.CDATA:
                            if (content == null) {
                                content = reader.getText().trim();
                            } else {
                                ignoreUnexpectedEvent(reader);
                            }
                            break;
                        case XMLStreamConstants.START_ELEMENT:
                            ignoreUnexpectedTag(reader);
                            break;
                        case XMLStreamConstants.END_ELEMENT:
                            checkEndTagMatch(reader, "text");
                            break OUTER;
                        default:
                            ignoreUnexpectedEvent(reader);
                            break;
                    }
                }
                if (content == null) {
                    content = "";
                }
                
                data.text = new TiledText(content, fontFamily, pixelSize, wrap, color,
                        bold, italic, underline, strikeout, kerning, hAlign, vAlign);
                break;
        }
    }
    
    private static void readObjectData(File file, XMLStreamReader reader, ObjectData data,
            List<PropertyObjectData> propertyObjectsToResolve) throws XMLStreamException {
        boolean propertiesRead = false;
        boolean shapeRead = false;
        OUTER: while (true) {
            next(reader);
            switch (reader.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "properties":
                            if (propertiesRead) {
                                ignoreRedundantTag(reader);
                            } else {
                                Map<String,Object> oldProperties = data.properties;
                                data.properties = readProperties(file, reader, propertyObjectsToResolve);
                                data.properties.putAll(oldProperties);
                            }
                            break;
                        case "ellipse":
                        case "point":
                        case "polygon":
                        case "polyline":
                        case "text":
                            if (shapeRead) {
                                ignoreRedundantTag(reader);
                            } else {
                                readShape(reader, data);
                                shapeRead = true;
                            }
                            break;
                        default:
                            ignoreUnexpectedTag(reader);
                            break;
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    checkEndTagMatch(reader, "object");
                    break OUTER;
                default:
                    ignoreUnexpectedEvent(reader);
                    break;
            }
        }
    }
    
    private static void readObject(File file, XMLStreamReader reader, List<TiledObject> objects,
            Map<Integer,TiledObject> allObjectsByID, List<PropertyObjectData> propertyObjectsToResolve,
            List<TileObjectToResolve> tileObjectsToResolve) throws XMLStreamException {
        //tileObjectsToResolve == null iff the object being read is one of a tile's collision objects
        Map<String,String> attributeValues = getAttributeValues(reader, OBJECT_ATTRIBUTES);
        
        int id = parseInt(reader, "id", attributeValues.get("id"));
        if (allObjectsByID.containsKey(id)) {
            ignoreRedundantTag(reader);
            return;
        }
        
        float x = parseFloat(reader, "x", attributeValues.get("x"));
        float y = parseFloat(reader, "y", attributeValues.get("y"));
        
        File templateFile = parseFile(file, reader, "template", attributeValues.get("template"), true, null);
        TiledObjectTemplate template;
        ObjectData data;
        if (templateFile == null) {
            template = null;
            data = new ObjectData();
        } else {
            template = getTemplate(templateFile.getPath());
            ensureReference(file, templateFile);
            data = new ObjectData(template);
        }
        
        updateObjectData(reader, data, attributeValues);
        readObjectData(file, reader, data, propertyObjectsToResolve);
        
        if (tileObjectsToResolve == null) {
            data.unresolvedGID = 0;
            data.tile = null;
        }
        TiledObject object = new TiledObject(data.name, data.type, x, y, data.width, data.height,
                data.rotation, data.tile, data.tileFlags, data.visible, data.shape, data.points, data.text,
                data.properties, template);
        objects.add(object);
        allObjectsByID.put(id, object);
        if (data.unresolvedGID != 0) {
            tileObjectsToResolve.add(new TileObjectToResolve(object, data.unresolvedGID));
        }
    }
    
    private static ObjectData readTemplateObject(File file, XMLStreamReader reader)
            throws XMLStreamException {
        Map<String,String> attributeValues = getAttributeValues(reader, TEMPLATE_OBJECT_ATTRIBUTES);
        ObjectData data = new ObjectData();
        updateObjectData(reader, data, attributeValues);
        readObjectData(file, reader, data, null);
        return data;
    }
    
    private static TiledImageLayer readImageLayer(File file, XMLStreamReader reader,
            List<TiledLayer> nonGroupLayers, Set<Integer> readLayerIDs, TiledGroupLayer parent,
            List<PropertyObjectData> propertyObjectsToResolve) throws XMLStreamException {
        Map<String,String> attributeValues = getAttributeValues(reader, IMAGELAYER_ATTRIBUTES);
        
        int id = parseInt(reader, "id", attributeValues.get("id"));
        if (!readLayerIDs.add(id)) {
            ignoreRedundantTag(reader);
            return null;
        }
        
        String name = attributeValues.get("name");
        int x = parseInt(reader, "x", attributeValues.get("x"), true, 0);
        int y = parseInt(reader, "y", attributeValues.get("y"), true, 0);
        float opacity = parseFloat(reader, "opacity", attributeValues.get("opacity"), true, 1);
        boolean visible = parseBooleanFromInt(reader, "visible", attributeValues.get("visible"), true, true);
        Color tintColor = parseColor(reader, "tintcolor", attributeValues.get("tintcolor"),
                true, DEFAULT_TINT_COLOR);
        float offsetX = parseFloat(reader, "offsetx", attributeValues.get("offsetx"), true, 0);
        float offsetY = parseFloat(reader, "offsety", attributeValues.get("offsety"), true, 0);
        
        Map<String,Object> properties = null;
        TiledImage image = null;
        OUTER: while (true) {
            next(reader);
            switch (reader.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "properties":
                            if (properties == null) {
                                properties = readProperties(file, reader, propertyObjectsToResolve);
                            } else {
                                ignoreRedundantTag(reader);
                            }
                            break;
                        case "image":
                            if (image == null) {
                                image = readImage(file, reader);
                            } else {
                                ignoreRedundantTag(reader);
                            }
                            break;
                        default:
                            ignoreUnexpectedTag(reader);
                            break;
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    checkEndTagMatch(reader, "imagelayer");
                    break OUTER;
                default:
                    ignoreUnexpectedEvent(reader);
                    break;
            }
        }
        
        TiledImageLayer layer = new TiledImageLayer(
                name, parent, opacity, visible, tintColor, offsetX, offsetY, image);
        layer.setProperties(properties);
        nonGroupLayers.add(layer);
        return layer;
    }
    
    private static TiledGroupLayer readGroup(File file, XMLStreamReader reader,
            List<TiledLayer> nonGroupLayers, Set<Integer> readLayerIDs, TiledGroupLayer parent,
            MapTileData tileData, Map<Integer,TiledObject> allObjectsByID,
            List<PropertyObjectData> propertyObjectsToResolve,
            List<TileObjectToResolve> tileObjectsToResolve) throws XMLStreamException {
        Map<String,String> attributeValues = getAttributeValues(reader, GROUP_ATTRIBUTES);
        
        int id = parseInt(reader, "id", attributeValues.get("id"));
        if (!readLayerIDs.add(id)) {
            ignoreRedundantTag(reader);
            return null;
        }
        
        String name = attributeValues.get("name");
        float opacity = parseFloat(reader, "opacity", attributeValues.get("opacity"), true, 1);
        boolean visible = parseBooleanFromInt(reader, "visible", attributeValues.get("visible"), true, true);
        Color tintColor = parseColor(reader, "tintcolor", attributeValues.get("tintcolor"),
                true, DEFAULT_TINT_COLOR);
        float offsetX = parseFloat(reader, "offsetx", attributeValues.get("offsetx"), true, 0);
        float offsetY = parseFloat(reader, "offsety", attributeValues.get("offsety"), true, 0);
        
        TiledGroupLayer group = new TiledGroupLayer(
                name, parent, opacity, visible, tintColor, offsetX, offsetY);
        
        Map<String,Object> properties = null;
        OUTER: while (true) {
            next(reader);
            switch (reader.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "properties":
                            if (properties == null) {
                                properties = readProperties(file, reader, propertyObjectsToResolve);
                            } else {
                                ignoreRedundantTag(reader);
                            }
                            break;
                        case "layer":
                            readLayer(file, reader, nonGroupLayers, readLayerIDs, group,
                                    tileData, propertyObjectsToResolve);
                            break;
                        case "objectgroup":
                            readMapObjectGroup(file, reader, nonGroupLayers, readLayerIDs, group,
                                    allObjectsByID, propertyObjectsToResolve, tileObjectsToResolve);
                            break;
                        case "imagelayer":
                            readImageLayer(file, reader, nonGroupLayers, readLayerIDs, group,
                                    propertyObjectsToResolve);
                            break;
                        case "group":
                            readGroup(file, reader, nonGroupLayers, readLayerIDs, group, tileData,
                                    allObjectsByID, propertyObjectsToResolve, tileObjectsToResolve);
                            break;
                        default:
                            ignoreUnexpectedTag(reader);
                            break;
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    checkEndTagMatch(reader, "group");
                    break OUTER;
                default:
                    ignoreUnexpectedEvent(reader);
                    break;
            }
        }
        
        group.finalizeChildren();
        group.setProperties(properties);
        nonGroupLayers.add(group);
        return group;
    }
    
    private static Map<String,Object> readProperties(File file, XMLStreamReader reader,
            List<PropertyObjectData> propertyObjectsToResolve) throws XMLStreamException {
        getAttributeValues(reader, Collections.emptyMap());
        Map<String,Object> properties = new HashMap<>();
        OUTER: while (true) {
            next(reader);
            switch (reader.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "property":
                            readProperty(file, reader, properties, propertyObjectsToResolve);
                            break;
                        default:
                            ignoreUnexpectedTag(reader);
                            break;
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    checkEndTagMatch(reader, "properties");
                    break OUTER;
                default:
                    ignoreUnexpectedEvent(reader);
                    break;
            }
        }
        return properties;
    }
    
    private static void readProperty(File file, XMLStreamReader reader, Map<String,Object> properties,
            List<PropertyObjectData> propertyObjectsToResolve) throws XMLStreamException {
        //propertyObjectsToResolve == null iff object properties are invalid in the current context
        //(e.g. if these are the properties of a tileset not embedded in a map)
        Map<String,String> attributeValues = getAttributeValues(reader, PROPERTY_ATTRIBUTES);
        
        String name = attributeValues.get("name");
        if (properties.containsKey(name)) {
            ignoreRedundantTag(reader);
            return;
        }
        
        String type = attributeValues.get("type");
        if (type == null) {
            type = "string";
        }
        String[] acceptableTypes = {"string", "int", "float", "bool", "color", "file", "object"};
        if (Arrays.asList(acceptableTypes).indexOf(type) == -1) {
            throwInvalidValueException(reader, "type", type,
                    "value must be one of the following: " + String.join(", ", acceptableTypes)
                            + ", or attribute must be absent");
        }
        
        String valueStr = attributeValues.get("value");
        
        OUTER: while (true) {
            next(reader);
            switch (reader.getEventType()) {
                case XMLStreamConstants.CHARACTERS:
                case XMLStreamConstants.CDATA:
                    if (valueStr == null) {
                        valueStr = reader.getText().trim();
                    } else {
                        ignoreUnexpectedEvent(reader);
                    }
                    break;
                case XMLStreamConstants.START_ELEMENT:
                    ignoreUnexpectedTag(reader);
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    checkEndTagMatch(reader, "property");
                    break OUTER;
                default:
                    ignoreUnexpectedEvent(reader);
                    break;
            }
        }
        
        try {
            switch (type) {
                case "string":
                    properties.put(name, (valueStr == null ? "" : valueStr));
                    break;
                case "int":
                    properties.put(name, parseInt(reader, "value", valueStr, true, 0));
                    break;
                case "float":
                    properties.put(name, parseFloat(reader, "value", valueStr, true, 0));
                    break;
                case "bool":
                    properties.put(name, parseBoolean(reader, "value", valueStr, true, false));
                    break;
                case "color":
                    properties.put(name, parseColor(reader, "value", valueStr, true, new Color(0, 0, 0, 0)));
                    break;
                case "file":
                    properties.put(name, parseFile(file, reader, "value", valueStr, true, new File(".")));
                    break;
                case "object":
                    if (propertyObjectsToResolve == null) {
                        throw new XMLStreamException(describeReaderLocation(reader) + ": <property> tag"
                                + " represents an object property in a context where object properties are"
                                + " invalid");
                    }
                    propertyObjectsToResolve.add(new PropertyObjectData(
                            properties, name, parseInt(reader, "value", valueStr, true, 0)));
                    break;
            }
        } catch (XMLStreamException e) {
            throw new XMLStreamException(describeReaderLocation(reader)
                + ": <property> tag (value type: " + type
                    + ") specifies an invalid value (" + valueStr + ")");
        }
    }
    
    private static TiledObjectTemplate readTemplate(File file, XMLStreamReader reader)
            throws XMLStreamException {
        getAttributeValues(reader, Collections.emptyMap());
        
        TiledTileset tileset = null;
        int firstGID = 0;
        ObjectData data = null;
        OUTER: while (true) {
            next(reader);
            switch (reader.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "tileset":
                            if (tileset == null) {
                                ReferencedTileset referencedTileset = readTXTileset(file, reader);
                                tileset = referencedTileset.tileset;
                                firstGID = referencedTileset.firstGID;
                            } else {
                                ignoreRedundantTag(reader);
                            }
                            break;
                        case "object":
                            if (data == null) {
                                data = readTemplateObject(file, reader);
                            } else {
                                ignoreRedundantTag(reader);
                            }
                            break;
                        default:
                            ignoreUnexpectedTag(reader);
                            break;
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    checkEndTagMatch(reader, "template");
                    break OUTER;
                default:
                    ignoreUnexpectedEvent(reader);
                    break;
            }
        }
        if (data == null) {
            throwMissingTagException(reader, "object");
        }
        
        TiledTile objectTile = null;
        if (data.unresolvedGID != 0) {
            if (tileset == null) {
                throw new XMLStreamException(describeReaderLocation(reader)
                        + ": <template> tag's <object> tag specifies a global tile ID,"
                                + " but the <template> tag contains no <tileset> tag");
            }
            try {
                objectTile = tileset.getTile(data.unresolvedGID - firstGID);
            } catch (IndexOutOfBoundsException e) {
                throw new XMLStreamException(describeReaderLocation(reader)
                        + ": <object> tag's global tile ID (" + data.unresolvedGID + ") is out of range");
            }
        }
        
        return new TiledObjectTemplate(file.getPath(),
                data.name, data.type, data.width, data.height, data.rotation, objectTile, data.tileFlags,
                data.visible, data.shape, data.points, data.text, data.properties);
    }
    
}
