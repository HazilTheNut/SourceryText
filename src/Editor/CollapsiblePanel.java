package Editor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by Jared on 3/23/2018.
 */
public class CollapsiblePanel extends JPanel{

    private boolean isActive = true;

    private Dimension normalSize;

    private final Color activeColor   = new Color(225, 225, 225);
    private final Color inactiveColor = new Color(210, 210, 210);
    private final Color hoveredColor  = new Color(184, 184, 184);
    private final Color borderColor   = new Color(170, 170, 170);

    private boolean mouseHovering = false;

    CollapsiblePanel(){
        MouseInput input = new MouseInput();
        addMouseListener(input);
        addMouseMotionListener(input);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        if (mouseHovering){
            g.setColor(hoveredColor);
        } else {
            if (isActive) g.setColor(activeColor);
            else g.setColor(inactiveColor);
        }
        g.fillRect(getBoxX(), getBoxY(), getBoxWidth(), getBoxHeight());
        g.setColor(borderColor);
        g.drawRect(getBoxX(), getBoxY(), getBoxWidth(), getBoxHeight());
        g.setColor(Color.BLACK);
        FontMetrics metrics = getFontMetrics(getFont());
        String str = (isActive) ? "-" : "+";
        g.drawString(str, getBoxX() + getBoxWidth()/2 - metrics.stringWidth(str)*2/5, getBoxY() + getBoxHeight()/2 + metrics.getHeight()*2/5 - 1);
    }

    private final int EDGE_MARGINS = 8;

    private int getBoxWidth() {
        return getWidth() - getBoxX() - EDGE_MARGINS - 1;
    }

    private int getBoxHeight() {
        FontMetrics metrics = getFontMetrics(getFont());
        return metrics.getHeight() - 2;
    }

    private int getBoxX(){
        return getWidth() - EDGE_MARGINS - 15;
    }

    private int getBoxY(){ return 1; }

    private boolean isMouseInCollapseButton(MouseEvent e){ return (e.getX() >= getBoxX() && e.getX() <= getBoxX() + getBoxWidth() && e.getY() >= getBoxY() && e.getY() <= getBoxY() + getBoxHeight());}

    public void setNormalSize(Dimension size) {
        normalSize = size;
        setMaximumSize(size);
    }

    private void activate(){
        for (Component component : getComponents()){
            component.setVisible(true);
        }
        setMaximumSize(normalSize);
        setPreferredSize(normalSize);
        isActive = true;
    }

    private void deactivate(){
        for (Component component : getComponents()){
            component.setVisible(false);
        }
        setMaximumSize(getMinimumSize());
        setPreferredSize(getMinimumSize());
        isActive = false;
    }

    private class MouseInput extends MouseAdapter{

        @Override
        public void mousePressed(MouseEvent e) {
            if (isMouseInCollapseButton(e)){
                if (isActive){
                    deactivate();
                } else {
                    activate();
                }
                repaint();
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            boolean currentHover = isMouseInCollapseButton(e);
            if (currentHover != mouseHovering)
                repaint();
            mouseHovering = currentHover;
        }
    }
}
