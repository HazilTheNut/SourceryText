package Editor.DrawTools;

import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;

import java.awt.*;

/**
 * Created by Jared on 2/25/2018.
 */
public class ArtLine extends ArtBrush {

    /**
     * ArtLine:
     *
     * A Tool that runs the Brush Tool in a line
     */

    private int startX;
    private int startY;

    private int previousX;
    private int previousY;

    private SpecialText startHighlight = new SpecialText(' ', Color.WHITE, new Color(75, 75, 255, 120));
    private LayerManager lm;

    public ArtLine(LayerManager manager) {
        lm = manager;
        name = "Line Tool";
        label = "Width: ";
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
    private void drawLine(Layer layer, int x1, int y1, int x2, int y2, SpecialText text){
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int distance = (int)Math.round(Math.sqrt(Math.pow((x2 - x1),2) + Math.pow((y2 - y1),2)));
        for (int ii = 0; ii <= distance; ii++){
            int col = (int)Math.round(ii * Math.cos(angle)) + x1;
            int row = (int)Math.round(ii * Math.sin(angle)) + y1;
            drawBrush(layer, col, row, text);
        }
    }
}
