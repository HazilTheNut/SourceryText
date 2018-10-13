package Start;

import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Engine.ViewWindow;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

/**
 * Created by Jared on 3/26/2018.
 */
public class FontSizeHeightStats {

    public static void main (String[] args) {

        JComponent dummy = new JComponent() {};

        System.out.println("FONT SIZE TEST: Font Size vs. Height");

        for (int i = 0; i < 100; i++) {
            Font font = new Font(Font.MONOSPACED, Font.PLAIN, i);
            FontMetrics metrics = dummy.getFontMetrics(font);
            System.out.printf("%1$d | %2$d\n", i, metrics.getHeight());
        }
    }
}
