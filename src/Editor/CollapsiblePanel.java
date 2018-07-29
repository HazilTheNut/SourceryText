package Editor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by Jared on 3/23/2018.
 */
public class CollapsiblePanel extends JPanel{

    /**
     * CollapsiblePanel:
     *
     * Turns out Swing doesn't have anything like this, which is weird.
     * This is VERY useful for what I am creating.
     *
     * The Art, Tiles, Entity, and Warp Zone tool panels are all collapsible.
     * This is because if you are using a small screen, some elements of the LevelEditor get obstructed. Therefore, the panels must be collapsible to become more space-saving.
     *
     * It's essentially a JPanel, but I've added a collapse button near the top.
     * Recommended to be given a border.
     */

    private boolean isActive = true;

    private Dimension normalSize;

    private final Color activeColor   = new Color(225, 225, 225); //Predefined colors
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
        super.paint(g); //Draw like a normal JPanel.
        //Below is the stuff concerning the collapse button
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
        //Look out! A pile of calculations that you probably hoped I would explain!
        g.drawString(str, getBoxX() + getBoxWidth()/2 - metrics.stringWidth(str)*2/5, getBoxY() + getBoxHeight()/2 + metrics.getHeight()*2/5 - 1);
    }

    private final int EDGE_MARGINS = 8;

    private int getBoxWidth() {
        return getWidth() - getBoxX() - EDGE_MARGINS - 3;
    }

    private int getBoxHeight() {
        FontMetrics metrics = getFontMetrics(getFont());
        return metrics.getHeight() - 2;
    }

    private int getBoxX(){
        return getWidth() - EDGE_MARGINS - 18;
    }

    private int getBoxY(){ return 1; }

    private boolean isMouseInCollapseButton(MouseEvent e){ return (e.getX() >= getBoxX() && e.getX() <= getBoxX() + getBoxWidth() && e.getY() >= getBoxY() && e.getY() <= getBoxY() + getBoxHeight());}

    //Sets the size to expand to when de-collapsing the panel.
    public void setNormalSize(Dimension size) {
        normalSize = size;
        setMaximumSize(size);
        setPreferredSize(size);
        setSize(size);
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

    @Override
    public Component add(Component comp) {
        comp.setVisible(isActive);
        return super.add(comp);
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
