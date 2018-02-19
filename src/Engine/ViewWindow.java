package Engine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Created by Jared on 2/18/2018.
 */
public class ViewWindow extends JComponent implements ComponentListener, MouseMotionListener, KeyListener{

    private Layer drawnImage;

    private int HOR_SEPARATION = 9;
    private int VER_SEPARATION = 16;
    private int CHAR_SIZE = 15;
    private int HOR_MARGIN = 0;
    private int VER_MARGIN = 0;

    private final int fontSizeAdjustment = 4;

    public final int RESOLUTION_WIDTH = 70;
    public final int RESOLUTION_HEIGHT = 30;

    private int mouseXCharPos = 0;
    private int mouseYCharPos = RESOLUTION_HEIGHT;

    private Font calculatedFont = new Font("Monospaced", Font.PLAIN, 15);

    public ViewWindow() {
        recalculate();
    }

    public void drawImage(Layer image){
        drawnImage = image;
        repaint();
    }

    private void recalculate() {
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
        g.fillRect(0, 0, getWidth(), getHeight()); //Create base background

        g.setFont(calculatedFont);

        for (int col = 0; col < RESOLUTION_WIDTH; col++) {
            for (int row = 0; row < RESOLUTION_HEIGHT; row++) {
                SpecialText text = drawnImage.getSpecialText(col, row);
                g.setColor(text.getBkgColor());
                g.fillRect(col * HOR_SEPARATION + HOR_MARGIN, row * VER_SEPARATION + VER_MARGIN, HOR_SEPARATION, VER_SEPARATION); //Fill background
                g.setColor(text.getFgColor());
                g.drawString(text.getStr(), col * HOR_SEPARATION + HOR_MARGIN + 2, VER_SEPARATION * (row+1) + VER_MARGIN - 5); //Fill foreground
            }
        }

        g.setColor(Color.GRAY); //Draw margin borders
        g.drawLine(HOR_MARGIN, 0, HOR_MARGIN, getHeight());
        g.drawLine(getWidth() - HOR_MARGIN, 0, getWidth() - HOR_MARGIN, getHeight());
        g.drawLine(0, VER_MARGIN, getWidth(), VER_MARGIN);
        g.drawLine(0, getHeight() - VER_MARGIN, getWidth(), getHeight() - VER_MARGIN);
    }

    @Override
    public void componentResized(ComponentEvent e) {
        recalculate();
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
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP){
            mouseYCharPos--;
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN){
            mouseYCharPos++;
        }
        System.out.println(mouseYCharPos);
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
