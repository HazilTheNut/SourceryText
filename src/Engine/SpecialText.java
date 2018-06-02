package Engine;

import Data.SerializationVersion;
import Game.Debug.DebugWindow;

import java.awt.*;
import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Created by Jared on 2/18/2018.
 */
public class SpecialText implements Serializable {

    /**
     * SpecialText:
     *
     * The basic unit of display for SourceryText.
     * It contains the following:
     *
     * character: The text character
     * fgColor:   The font color
     * bkgColor:  The highlight color
     *
     * In most application, the basic unit is a pixel.
     * The SpecialText is very similar to that, although much bigger [citation needed].
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private char character;
    private Color fgColor =  new Color(255, 255, 255, 255);
    private Color bkgColor = new Color(0,   0,   0,   0);

    public SpecialText (char text) {
        character = text;
    }

    public SpecialText (char text, Color fg){
        character = text;
        fgColor = fg;
    }

    public SpecialText (char text, Color fg, Color bg){
        character = text;
        fgColor = fg;
        bkgColor = bg;
    }

    public char getCharacter() {
        return character;
    }

    public String getStr() {return String.valueOf(character); }

    public Color getFgColor() {
        return fgColor;
    }

    public Color getBkgColor() {
        return bkgColor;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof SpecialText){
            SpecialText text = (SpecialText)obj;
            return text.getCharacter() == character && text.getFgColor().equals(fgColor) && text.getBkgColor().equals(bkgColor);
        }
        return false;
    }

    /**
     * Can be treated like equals(), except the comparison does not care for color alpha.
     *
     * @param text The other SpecialText to compare to.
     * @return Whether or not the two SpecialText's are 'similar'
     */
    public boolean similar(SpecialText text) {
        return text != null && text.getCharacter() == character && text.getFgColor().getRed() == fgColor.getRed()   && text.getFgColor().getGreen() == fgColor.getGreen()   && text.getFgColor().getBlue() == fgColor.getBlue()
                                                                && text.getBkgColor().getRed() == bkgColor.getRed() && text.getBkgColor().getGreen() == bkgColor.getGreen() && text.getBkgColor().getBlue() == bkgColor.getBlue();
    }

    @Override
    public String toString() {
        return String.format("|%1$c|[%2$03d,%3$03d,%4$03d,%5$03d],[%6$03d,%7$03d,%8$03d,%9$03d]", getCharacter(), fgColor.getRed(), fgColor.getGreen(), fgColor.getBlue(), fgColor.getAlpha(), bkgColor.getRed(), bkgColor.getGreen(), bkgColor.getBlue(), bkgColor.getAlpha());
    }

    public void transpose(SpecialText text){
        character = text.getCharacter();
        fgColor = text.getFgColor();
        bkgColor = text.getBkgColor();
    }

    /**
     * Generates a SpecialText from the output of SpecialText.toString()
     *
     * Here is the format:
     * |C|[000,000,000,000],[000,000,000,000]
     *
     * where 'C' is a char
     * and a set of zeroes is an integer. There must the exactly as many digits in the number as there are zeroes.
     *
     * @param text The String to convert into a SpecialText
     * @return The now-converted SpecialText. Returns null if input string is malformed
     */
    public static SpecialText fromString(String text){
        char c = text.charAt(1);
        try {
            int fr = readInt(text.substring(4, 7));
            int fg = readInt(text.substring(8, 11));
            int fb = readInt(text.substring(12, 15));
            int fa = readInt(text.substring(16, 19));
            int br = readInt(text.substring(22, 25));
            int bg = readInt(text.substring(26, 29));
            int bb = readInt(text.substring(30, 33));
            int ba = readInt(text.substring(34, 37));
            return new SpecialText(c, new Color(fr, fg, fb, fa), new Color(br, bg, bb, ba));
        } catch ( NoSuchElementException | IllegalStateException e){
            DebugWindow.reportf(DebugWindow.MISC, "SpecialText.fromString", "Error: %1$s", e.getMessage());
            return null;
        }
    }

    private static int readInt(String str){
        Scanner sc = new Scanner(str);
        return sc.nextInt();
    }
}
