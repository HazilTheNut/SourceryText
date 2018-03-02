package Engine;

import java.awt.*;
import java.io.Serializable;

/**
 * Created by Jared on 2/18/2018.
 */
public class SpecialText implements Serializable{

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
        if (obj instanceof SpecialText){
            SpecialText text = (SpecialText)obj;
            return text.getCharacter() == character && text.getFgColor().equals(fgColor) && text.getBkgColor().equals(bkgColor);
        }
        return false;
    }
}
