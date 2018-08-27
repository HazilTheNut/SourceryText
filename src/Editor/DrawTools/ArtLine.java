package Editor.DrawTools;

import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Jared on 2/25/2018.
 */
public class ArtLine extends DrawTool {

    /**
     * ArtLine:
     *
     * A Tool that fills in an area defined by a line and a width.
     *
     * ArtBrush and ArtEraser inherit this class in order to fill in any gaps when the mouse moves very quickly.
     */

    private int startX;
    private int startY;

    private int previousX;
    private int previousY;

    private SpecialText startHighlight = new SpecialText(' ', Color.WHITE, new Color(75, 75, 255, 120));
    private LayerManager lm;

    private JSpinner brushSizeBox;
    String name;
    String label;

    public ArtLine(LayerManager manager) {
        lm = manager;
        name = "Line Tool";
        label = "Width: ";
    }

    @Override
    public void onActivate(JPanel panel) {
        brushSizeBox = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
        brushSizeBox.setMaximumSize(new Dimension(50, 20));
        panel.setBorder(BorderFactory.createTitledBorder(name));
        JLabel boxLabel = new JLabel(label);
        //panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        panel.add(boxLabel);
        panel.add(brushSizeBox);
        panel.validate();
        panel.setVisible(true);

        TOOL_TYPE = DrawTool.TYPE_ART;
    }

    @Override
    public void onDrawStart(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        startX = col;
        startY = row;
    }

    @Override
    public void onDraw(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        int xOffset = -lm.getCameraPos().getX() + layer.getX(); //Corrections need to be made for the camera.
        int yOffset = -lm.getCameraPos().getY() + layer.getY(); //It does also do corrections for the backdrop layer moving around, but the Level's backdrop does ever move.
        drawLine(highlight, startX + xOffset, startY + yOffset, previousX + xOffset, previousY + yOffset, null); //Draw a line of null characters over the previous line
        drawLine(highlight, startX + xOffset, startY + yOffset, col + xOffset, row + yOffset, startHighlight); //Draw the new line
        previousX = col;
        previousY = row;
    }

    @Override
    public void onDrawEnd(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        highlight.clearLayer(); //Get rid of preview highlight
        drawLine(layer, startX, startY, col, row, text);
    }

    @Override
    public void onCancel(Layer highlight, int col, int row) {
        highlight.clearLayer();
    }

    /*
    * There are many line algorithms out there. This is probably one of them.
    * I'm just lazily using Math.atan2() to find the angle between point 1 and 2, and then drawing a line.
    * The iterator gets the hypotenuse-distance between the two points to figure out how many iterations to go for.
    */
    void drawLine(Layer layer, int x1, int y1, int x2, int y2, SpecialText text){
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int distance = (int)Math.round(Math.sqrt(Math.pow((x2 - x1),2) + Math.pow((y2 - y1),2)));
        for (int ii = 0; ii <= distance; ii++){
            int col = (int)Math.round(ii * Math.cos(angle)) + x1;
            int row = (int)Math.round(ii * Math.sin(angle)) + y1;
            drawBrush(layer, col, row, text);
        }
    }

    /**
     * Draws a square diamond-shape at a cursor position. The length from one corner to the other is (2 * brushSize) - 1
     * @param layer The layer to draw on
     * @param centerCol The x position of the center of the diamond
     * @param centerRow The y position of the center of the diamond
     * @param text The SpecialText to draw with.
     */
    void drawBrush(Layer layer, int centerCol, int centerRow, SpecialText text){
        int brushSize = 1;
        try {
            brushSize = ((SpinnerNumberModel)brushSizeBox.getModel()).getNumber().intValue();
        } catch (NumberFormatException ignored) {}
        if (brushSize <= 0 ) brushSize = 1;
        for (int x = 0; x < brushSize; x++){
            int height = brushSize - x - 1;
            for (int row = centerRow + height; row >= centerRow - height; row--){
                layer.editLayer(centerCol + x, row, text);
                layer.editLayer(centerCol - x, row, text);
            }
        }
    }
}
