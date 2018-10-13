package Editor;

import Engine.SpecialText;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;

public class SpecialTextList extends JComponent implements MouseInputListener, MouseWheelListener {

    /**
     * This class defines a custom version of the Text Panel on the left side of the level editor. It work mirror the function of the scroll pane of buttons currently in use.
     * After creating it, I found very little benefit in fully implementing it.
     */

    private ArrayList<SpecialText> textList;
    private int listWidth;
    private final float HEIGHT_WIDTH_RATIO = 9f / 13;
    private final float FONTSIZE_CHARHEIGHT_RATIO = 99f / 130; //This has been scientifically tested.

    private int specTxtWidth;
    private int specTxtHeight;
    private int selectedIndex;

    private int scrollYOffset;

    public SpecialTextList(int width){
        listWidth = width;
        textList = new ArrayList<>();
    }

    public void setTextList(ArrayList<SpecialText> textList) {
        this.textList = textList;
    }

    @Override
    public void paintComponent(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        specTxtWidth = getWidth() / listWidth;
        specTxtHeight = (int)((specTxtWidth) * HEIGHT_WIDTH_RATIO);
        int fontSize = (int) Math.max(2, (specTxtHeight - 2) * FONTSIZE_CHARHEIGHT_RATIO);
        //Generate font
        Font font = new Font(Font.MONOSPACED, Font.PLAIN, fontSize);
        g.setFont(font);
        FontMetrics fontMetrics = g.getFontMetrics();
        int charWidth = fontMetrics.charWidth('A');
        int charHeight = fontMetrics.getHeight() / 2;
        System.out.printf("[SpecialTextList] char w %1$d h %2$d spectxt w %3$d h %4$d fontsize %5$d\n", charWidth, charHeight, specTxtWidth, specTxtHeight, fontSize);
        //Begin drawing
        int numRows = getHeight() / specTxtHeight;
        for (int index = scrollYOffset * listWidth; index <= Math.min(textList.size() - 1, (numRows + scrollYOffset) * listWidth); index++) {
            int xLoc = index % listWidth;
            int yLoc = (index - xLoc) / listWidth - scrollYOffset;
            int drawX = xLoc * specTxtWidth;
            int drawY = yLoc * specTxtHeight;
            SpecialText fromList = textList.get(index);
            if (fromList != null) {
                g.setColor(fromList.getBkgColor());
                g.fillRect(drawX, drawY, specTxtWidth, specTxtHeight);
                g.setColor(fromList.getFgColor()); // + (specTxtWidth - charWidth) / 2 : - (specTxtHeight - charHeight) / 2
                g.drawString(fromList.getStr(), drawX + (specTxtWidth - charWidth) / 2, drawY + specTxtHeight - (specTxtHeight - charHeight) / 2);
                if (index == selectedIndex){
                    g.setColor(Color.WHITE);
                    g.drawRect(drawX, drawY, specTxtWidth - 1, specTxtHeight - 1);
                } else if (index == prevHoveredIndex){
                    g.setColor(Color.LIGHT_GRAY);
                    g.drawRect(drawX, drawY, specTxtWidth - 1, specTxtHeight - 1);
                }
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    private int getIndexMousePos(int rawX, int rawY){
        int gridLocX = rawX / specTxtWidth;
        int gridLocY = rawY / specTxtHeight + scrollYOffset;
        return (gridLocY * listWidth) + gridLocX;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int index = getIndexMousePos(e.getX(), e.getY());
        if (index >= 0 && index < textList.size()) {
            selectedIndex = index;
            System.out.printf("[SpecialTextList] final index: %1$d\n", selectedIndex);
            repaint();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        scrollYOffset += (int)e.getPreciseWheelRotation();
        scrollYOffset = Math.min(scrollYOffset, textList.size() - (getHeight() / specTxtHeight) * listWidth);
        scrollYOffset = Math.max(0, scrollYOffset);
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    private int prevHoveredIndex = 0;

    private void updateHoverIndex(int mouseX, int mouseY){
        int index = getIndexMousePos(mouseX, mouseY);
        if (index != prevHoveredIndex){
            prevHoveredIndex = index;
            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        updateHoverIndex(e.getX(), e.getY());
    }
}
