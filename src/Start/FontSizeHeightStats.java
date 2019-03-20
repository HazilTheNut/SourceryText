package Start;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Jared on 3/26/2018.
 */
public class FontSizeHeightStats {

    public static void main (String[] args) {

        JComponent dummy = new JComponent() {};

        System.out.println("FONT SIZE TEST: Font Size vs. Height");

        float avgFontHeightPerSize = 0;
        float numCycles = 100;

        for (int i = 0; i < numCycles; i++) {
            Font font = new Font(Font.MONOSPACED, Font.PLAIN, i);
            FontMetrics metrics = dummy.getFontMetrics(font);
            System.out.printf("%1$d | %2$d\n", i, metrics.getHeight());
            if (i > 0)
                avgFontHeightPerSize += ((float)metrics.getHeight() / i) / (numCycles - 1);
        }

        System.out.printf("\nAvg. Font height per size: %1$.3f\n", avgFontHeightPerSize);
    }
}
