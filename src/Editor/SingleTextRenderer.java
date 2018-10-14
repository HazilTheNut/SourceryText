package Editor;

import Engine.SpecialText;

import javax.swing.*;
import java.awt.*;

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

    SingleTextRenderer(SpecialText text) {
        specText = text;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        if (specText != null) {
            g.setColor(specText.getBkgColor());
            g.fillRect(x, y, getIconWidth(), getIconHeight());
            g.setColor(specText.getFgColor());
            g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 17));
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
