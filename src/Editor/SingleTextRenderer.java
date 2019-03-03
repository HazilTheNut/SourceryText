package Editor;

import Data.FileIO;
import Engine.SpecialText;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Created by Jared on 2/25/2018.
 */
class SingleTextRenderer implements Icon {

    /**
     * SingleTextRenderer:
     *
     * An Icon that draws a singular SpecialText, complete with font and highlight color.
     */

    SpecialText specText;
    private Font font;

    SingleTextRenderer(SpecialText text) {
        specText = text;
        FileIO io = new FileIO();
        File fontFile = new File(io.getRootFilePath() + "font.ttf");
        if (fontFile.exists()) {
            try {
                font = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(17f);
            } catch (FontFormatException | IOException e) {
                e.printStackTrace();
            }
        }
        if (font == null) {
            font = new Font(Font.MONOSPACED, Font.PLAIN, 17);
        }
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        if (specText != null) {
            g.setColor(specText.getBkgColor());
            g.fillRect(x, y, getIconWidth(), getIconHeight());
            g.setColor(specText.getFgColor());
            g.setFont(font);
            g.drawString(specText.getStr(), x + (getIconWidth() / 2) - 5, y + (getIconHeight() - 4));
        } /*else {
            g.setColor(Color.WHITE);
            g.fillRect(x, y, getIconWidth(), getIconHeight());
            g.setColor(Color.BLACK);
            g.drawRect(x, y, getIconWidth() - 1, getIconHeight() - 1);
        }
        /**/
    }

    @Override
    public int getIconWidth() {
        return 19;
    }

    @Override
    public int getIconHeight() {
        return 19;
    }
}
