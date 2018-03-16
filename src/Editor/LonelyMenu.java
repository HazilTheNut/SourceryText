package Editor;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * Created by Jared on 3/10/2018.
 */
public class LonelyMenu extends JComponent implements MouseInputListener {

    private boolean mouseHovering = false;
    private JPopupMenu menu;

    LonelyMenu(JPopupMenu popupMenu, Container c){
        menu = popupMenu;
        setMaximumSize(new Dimension(100, 15));
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
        g.drawString(menu.getLabel(), (getWidth() - nameLength)/2, getHeight()-3);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        //menu.show(this, -129, -2);
        menu.show(this, -1 * (int)menu.getPreferredSize().getWidth() - 2, -2);
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
