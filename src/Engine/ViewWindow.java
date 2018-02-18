package Engine;

import javafx.scene.input.KeyCode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Created by Jared on 2/18/2018.
 */
public class ViewWindow extends JComponent implements ComponentListener, MouseMotionListener, KeyListener{

    private int HOR_SEPARATION = 9;
    private int VER_SEPARATION = 16;
    private int CHAR_SIZE = 15;
    private int HOR_MARGIN = 0;
    private int VER_MARGIN = 0;

    private final int fontSizeAdjustment = 4;

    private final int RESOLUTION_WIDTH = 75;
    private final int RESOLUTION_HEIGHT = 40;

    private int mouseXCharPos = 0;
    private int mouseYCharPos = RESOLUTION_HEIGHT;

    private Font calculatedFont = new Font("Monospaced", Font.PLAIN, 15);

    public ViewWindow() {
        recalculate();
    }

    private void recalculate() {
        /*
        HOR_SEPARATION = getWidth() / RESOLUTION_WIDTH; //Calculates horizontal and vertical separation of the letters
        VER_SEPARATION = Math.round((float)getHeight() / RESOLUTION_HEIGHT);
        int adjustedVerSep = (int) ((float) VER_SEPARATION * (9f / 15)); //Adjusts horizontal separation if too big for vertical.
        //if (HOR_SEPARATION > adjustedVerSep) {
            HOR_SEPARATION = adjustedVerSep;
            HOR_MARGIN = (getWidth() - (HOR_SEPARATION * RESOLUTION_WIDTH)) / 2; //Sets a margin to center display in the screen
        //} else {
        //    HOR_MARGIN = 0;
        //}
        CHAR_SIZE = VER_SEPARATION - 1; //Calculations based upon horizontal and vertical separation
        CHAR_WIDTH = HOR_SEPARATION;
        CHAR_HEIGHT = CHAR_SIZE + 1;

        System.out.println(getHeight());

        */

        double MAX_VER_HOR_SEPARATION_RATIO = 0.6;

        HOR_SEPARATION = (int)Math.floor((double)getWidth() / RESOLUTION_WIDTH);
        VER_SEPARATION = (int)Math.floor((double)getHeight() / RESOLUTION_HEIGHT);
        if (HOR_SEPARATION > VER_SEPARATION * MAX_VER_HOR_SEPARATION_RATIO) HOR_SEPARATION = (int)(VER_SEPARATION * MAX_VER_HOR_SEPARATION_RATIO);
        if (HOR_SEPARATION < VER_SEPARATION) CHAR_SIZE = HOR_SEPARATION;
        else CHAR_SIZE = VER_SEPARATION;

        int displayLength = HOR_SEPARATION * RESOLUTION_WIDTH;
        int displayHeight = VER_SEPARATION * RESOLUTION_HEIGHT;
        HOR_MARGIN = (getWidth() - displayLength) / 2;
        VER_MARGIN = (getHeight() - displayHeight) / 2;

        calculatedFont = (new Font(Font.MONOSPACED, Font.PLAIN, CHAR_SIZE + fontSizeAdjustment));
    }

    @Override
    public void paintComponent(Graphics g) {

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(Color.LIGHT_GRAY);
        if (mouseXCharPos >= 0 && mouseXCharPos < 15 && mouseYCharPos >= 0 && mouseYCharPos < RESOLUTION_HEIGHT)
        g.fillRect(HOR_MARGIN, mouseYCharPos * VER_SEPARATION + VER_MARGIN, HOR_SEPARATION * 15, VER_SEPARATION);

        g.setFont(calculatedFont);

        for (int col = 0; col < 15; col++) {
            for (int row = 1; row <= RESOLUTION_HEIGHT; row++) {
                /*
                if (row == 1){
                    g.setColor(new Color((int)Math.abs(Math.sin(col) * 250), (int)Math.abs(Math.sin(col) * 250), 25));
                    g.fillRect(col * HOR_SEPARATION + HOR_MARGIN, (row-1) * VER_SEPARATION + VER_MARGIN, HOR_SEPARATION, VER_SEPARATION);
                } else if (col % 2 == row % 2) {
                    g.setColor(Color.LIGHT_GRAY);
                    g.fillRect(col * HOR_SEPARATION + HOR_MARGIN, (row-1) * VER_SEPARATION + VER_MARGIN, HOR_SEPARATION, VER_SEPARATION);
                }
                */
                g.setColor(Color.YELLOW);
                g.drawString("#", col * HOR_SEPARATION + HOR_MARGIN + 2, VER_SEPARATION * row + VER_MARGIN - 5);
            }
        }

        g.setColor(Color.GRAY);
        g.drawLine(HOR_MARGIN, 0, HOR_MARGIN, getHeight());
        g.drawLine(getWidth() - HOR_MARGIN, 0, getWidth() - HOR_MARGIN, getHeight());
        g.drawLine(0, VER_MARGIN, getWidth(), VER_MARGIN);
        g.drawLine(0, getHeight() - VER_MARGIN, getWidth(), getHeight() - VER_MARGIN);
    }

    @Override
    public void componentResized(ComponentEvent e) {
        recalculate();
        repaint();
    }

    @Override
    public void componentMoved(ComponentEvent e) {

    }

    @Override
    public void componentShown(ComponentEvent e) {

    }

    @Override
    public void componentHidden(ComponentEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        //System.out.println(String.format("New Mouse Pos: %1$d,%2$d", e.getX(), e.getY()));
        mouseXCharPos = (e.getX() - HOR_MARGIN) / HOR_SEPARATION;
        mouseYCharPos = (e.getY() - VER_MARGIN) / VER_SEPARATION;
        System.out.println(String.format("New Mouse Pos: %1$d,%2$d", mouseXCharPos, mouseYCharPos));
        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP){
            mouseYCharPos--;
            repaint();
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN){
            mouseYCharPos++;
            repaint();
        }
        System.out.println(mouseYCharPos);
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
