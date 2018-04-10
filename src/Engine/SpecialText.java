package Engine;

import Data.SerializationVersion;

import java.awt.*;
import java.io.Serializable;

/**
 * Created by Jared on 2/18/2018.
 */
public class SpecialText implements Serializable{

    private static final long serialVersionUID = SerializationVersion.LEVELDATA_SERIALIZATION_VERSION;

    private char character = ' ';
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

    // A lot like equals(), except the comparison ignores alpha
    public boolean similar(SpecialText text) {
        return text != null && text.getCharacter() == character && text.getFgColor().getRed() == fgColor.getRed()   && text.getFgColor().getGreen() == fgColor.getGreen()   && text.getFgColor().getBlue() == fgColor.getBlue()
                                                                && text.getBkgColor().getRed() == bkgColor.getRed() && text.getBkgColor().getGreen() == bkgColor.getGreen() && text.getBkgColor().getBlue() == bkgColor.getBlue();
    }

    @Override
    public String toString() {
        return String.format("%1$c [%2$d,%3$d,%4$d,%5$d] [%6$d,%7$d,%8$d,%9$d]", getCharacter(), fgColor.getRed(), fgColor.getGreen(), fgColor.getBlue(), fgColor.getAlpha(), bkgColor.getRed(), bkgColor.getGreen(), bkgColor.getBlue(), bkgColor.getAlpha());
    }

    public void transpose(SpecialText text){
        character = text.getCharacter();
        fgColor = text.getFgColor();
        bkgColor = text.getBkgColor();
    }
}
