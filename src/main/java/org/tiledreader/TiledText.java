package org.tiledreader;

import java.awt.Color;

/**
 * <p>A TiledText represents the text of a text-type Tiled object. It
 * corresponds to a &lt;text&gt; tag in a Tiled XML file.</p>
 * @author Alex Heyman
 */
public class TiledText {
    
    /**
     * <p>Represents a horizontal alignment that text can have within a text
     * object.</p>
     */
    public static enum HAlign {
        LEFT, CENTER, RIGHT, JUSTIFY
    }
    
    /**
     * <p>Represents a vertical alignment that text can have within a text
     * object.</p>
     */
    public static enum VAlign {
        TOP, CENTER, BOTTOM
    }
    
    private final String content;
    private final String fontFamily;
    private final int pixelSize;
    private final boolean wrap;
    private final Color color;
    private final boolean bold, italic, underline, strikeout, kerning;
    private final HAlign hAlign;
    private final VAlign vAlign;
    
    TiledText(String content, String fontFamily, int pixelSize, boolean wrap, Color color,
            boolean bold, boolean italic, boolean underline, boolean strikeout, boolean kerning,
            HAlign hAlign, VAlign vAlign) {
        this.content = content;
        this.fontFamily = fontFamily;
        this.pixelSize = pixelSize;
        this.wrap = wrap;
        this.color = color;
        this.bold = bold;
        this.italic = italic;
        this.underline = underline;
        this.strikeout = strikeout;
        this.kerning = kerning;
        this.hAlign = hAlign;
        this.vAlign = vAlign;
    }
    
    /**
     * Returns the content of this text; that is, the actual text.
     * @return The content of this text
     */
    public final String getContent() {
        return content;
    }
    
    /**
     * Returns this text's font family ("sans-serif" by default).
     * @return This text's font family
     */
    public final String getFontFamily() {
        return fontFamily;
    }
    
    /**
     * Returns this text's font size in pixels (16 by default).
     * @return This text's font size in pixels
     */
    public final int getPixelSize() {
        return pixelSize;
    }
    
    /**
     * Returns whether word wrapping is enabled for this text (false by
     * default).
     * @return Whether word wrapping is enabled for this text
     */
    public final boolean getWrap() {
        return wrap;
    }
    
    /**
     * Returns this text's color (black by default).
     * @return This text's color
     */
    public final Color getColor() {
        return color;
    }
    
    /**
     * Returns whether this text's font is bold (false by default).
     * @return Whether this text's font is bold
     */
    public final boolean getBold() {
        return bold;
    }
    
    /**
     * Returns whether this text's font is italic (false by default).
     * @return Whether this text's font is italic
     */
    public final boolean getItalic() {
        return italic;
    }
    
    /**
     * Returns whether a line should be drawn below this text (false by
     * default).
     * @return Whether a line should be drawn below this text
     */
    public final boolean getUnderline() {
        return underline;
    }
    
    /**
     * Returns whether a line should be drawn through this text (false by
     * default).
     * @return Whether a line should be drawn below this text
     */
    public final boolean getStrikeout() {
        return strikeout;
    }
    
    /**
     * Returns whether kerning should be used while rendering this text (true by
     * default).
     * @return Whether kerning should be used while rendering this text
     */
    public final boolean getKerning() {
        return kerning;
    }
    
    /**
     * Returns this text's horizontal alignment within its text object
     * (<code>TiledText.HAlign.LEFT</code>) by default.
     * @return This text's horizontal alignment within its text object
     */
    public final HAlign getHAlign() {
        return hAlign;
    }
    
    /**
     * Returns this text's vertical alignment within its text object
     * (<code>TiledText.VAlign.TOP</code>) by default.
     * @return This text's vertical alignment within its text object
     */
    public final VAlign getVAlign() {
        return vAlign;
    }
    
}
