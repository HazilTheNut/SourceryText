package Engine;

import java.awt.*;

/**
 * Created by Jared on 2/18/2018.
 */
public class SpecialText {

    private char character = ' ';
    private Color fgColor =  new Color(255, 255, 255, 255);
    private Color bkgColor = new Color(0,   0,   0,   0);

    public boolean opaque;

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

    public SpecialText (char text, Color fg, Color bg, boolean opacity){
        character = text;
        fgColor = fg;
        bkgColor = bg;
        opaque = opacity;
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
}
