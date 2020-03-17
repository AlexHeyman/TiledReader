package org.tiledreader;

import java.awt.Color;

/**
 *
 * @author Alex Heyman
 */
public class TiledText {
    
    public static enum HAlign {
        LEFT, CENTER, RIGHT, JUSTIFY
    }
    
    public static enum VAlign {
        TOP, CENTER, BOTTOM
    }
    
    private final String content;
    private final String fontFamily;
    private final int pixelSize;
    private final boolean wrap;
    private final Color color;
    private final boolean bold, italic, strikeout, underline, kerning;
    private final HAlign hAlign;
    private final VAlign vAlign;
    
    TiledText(String content, String fontFamily, int pixelSize, boolean wrap, Color color,
            boolean bold, boolean italic, boolean strikeout, boolean underline, boolean kerning,
            HAlign hAlign, VAlign vAlign) {
        this.content = content;
        this.fontFamily = fontFamily;
        this.pixelSize = pixelSize;
        this.wrap = wrap;
        this.color = color;
        this.bold = bold;
        this.italic = italic;
        this.strikeout = strikeout;
        this.underline = underline;
        this.kerning = kerning;
        this.hAlign = hAlign;
        this.vAlign = vAlign;
    }
    
    public final String getContent() {
        return content;
    }
    
    public final String getFontFamily() {
        return fontFamily;
    }
    
    public final int getPixelSize() {
        return pixelSize;
    }
    
    public final boolean getWrap() {
        return wrap;
    }
    
    public final Color getColor() {
        return color;
    }
    
    public final boolean getBold() {
        return bold;
    }
    
    public final boolean getItalic() {
        return italic;
    }
    
    public final boolean getStrikeout() {
        return strikeout;
    }
    
    public final boolean getUnderline() {
        return underline;
    }
    
    public final boolean getKerning() {
        return kerning;
    }
    
    public final HAlign getHAlign() {
        return hAlign;
    }
    
    public final VAlign getVAlign() {
        return vAlign;
    }
    
}
