package Editor;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * Created by Jared on 3/10/2018.
 */
public class LonelyMenu extends JComponent implements MouseInputListener {

    /**
     * LonelyMenu:
     *
     * JMenu's require being placed in a JMenuBar, which wastes a bunch of space.
     * So therefore, custom component!
     *
     * Basically, it's a menu button without needing to be in a menu bar.
     */

    private boolean mouseHovering = false;
    private JPopupMenu menu;

    LonelyMenu(JPopupMenu popupMenu, Container c){
        setAlignmentX(CENTER_ALIGNMENT);
        menu = popupMenu;
        setPreferredSize(new Dimension(100, 15));
        c.addMouseListener(this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (mouseHovering) {
            g.setColor(new Color(205, 205, 205));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        FontMetrics fontMetrics = g.getFontMetrics();
        int nameLength = fontMetrics.stringWidth(menu.getLabel());
        g.setColor(Color.BLACK);
        //Keeps the text centered.
        g.drawString(menu.getLabel(), (getWidth() - nameLength)/2, getHeight()-3);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (menu.isShowing())
            menu.setVisible(false);
        else
            menu.show(this, -1 * (int)menu.getPreferredSize().getWidth() - 2, -2); //JPopupMenu has the origin of its list at the top-left corner, so things have to be shifted around.
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        mouseHovering = true;
        repaint();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        mouseHovering = false;
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }
}
