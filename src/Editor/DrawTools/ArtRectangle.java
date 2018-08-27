package Editor.DrawTools;

import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Jared on 2/25/2018.
 */
public class ArtRectangle extends DrawTool {

    /**
     * ArtRectangle:
     *
     * A Tool that draws / fills a rectangle based on two corner points
     */

    private int startX;
    private int startY;

    private int previousX;
    private int previousY;

    private SpecialText startHighlight = new SpecialText(' ', Color.WHITE, new Color(75, 75, 255, 120));
    LayerManager lm;

    private JCheckBox fillBox;

    public ArtRectangle(LayerManager manager) {lm = manager; }

    @Override
    public void onActivate(JPanel panel) {
        fillBox = new JCheckBox("Filled", false); //Allow input for switching between filled and empty rectangles
        //panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS)); //BoxLayout does not pack down like BorderLayout, ensuring consistent panel sizes between tools.
        panel.add(fillBox);
        panel.setBorder(BorderFactory.createTitledBorder("Rectangle"));
        panel.setVisible(true);
        panel.validate();

        TOOL_TYPE = TYPE_ART;
    }

    @Override
    public void onDrawStart(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        startX = col;
        startY = row;
    }

    @Override
    public void onDraw(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        int xOffset = -lm.getCameraPos().getX() + layer.getX(); //Mentioned in ArtLine, but the backdrop doesn't change its position, but JUST IN CASE.....
        int yOffset = -lm.getCameraPos().getY() + layer.getY();
        drawRect(highlight, startX + xOffset, startY + yOffset, previousX + xOffset, previousY + yOffset, null, fillBox.isSelected()); //Clear the previous rectangle
        drawRect(highlight, startX + xOffset, startY + yOffset, col + xOffset, row + yOffset, startHighlight, fillBox.isSelected()); //Make a new one
        previousX = col;
        previousY = row;
        //System.out.println("[ArtLine] onDraw");
    }

    @Override
    public void onDrawEnd(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        highlight.clearLayer();
        drawRect(layer, startX, startY, col, row, text, fillBox.isSelected());
    }

    @Override
    public void onCancel(Layer highlight, int col, int row) {
        highlight.clearLayer();
    }

    void drawRect(Layer layer, int x1, int y1, int x2, int y2, SpecialText text, boolean isFilled){
        int colSign = (x2 > x1) ? 1 : -1; //The two corners can be in any position in relation to each other, so that better be accounted for.
        int rowSign = (y2 > y1) ? 1 : -1;
        if (isFilled){ //Do the filled rectangle
            for (int col = x1; col*colSign <= x2*colSign; col+=colSign){
                for (int row = y1; row*rowSign <= y2*rowSign; row+=rowSign){
                    layer.editLayer(col, row, text);
                }
            }
        } else { //Do the not filled rectangle
            for (int col = x1; col*colSign <= x2*colSign; col+=colSign) {
                layer.editLayer(col, y1, text);
                layer.editLayer(col, y2, text);
            }
            for (int row = y1; row*rowSign <= y2*rowSign; row+=rowSign) {
                layer.editLayer(x1, row, text);
                layer.editLayer(x2, row, text);
            }
        }
    }
}
