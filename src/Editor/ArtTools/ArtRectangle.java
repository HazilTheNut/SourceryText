package Editor.ArtTools;

import Engine.Layer;
import Engine.SpecialText;

import java.awt.*;

/**
 * Created by Jared on 2/25/2018.
 */
public class ArtRectangle extends ArtTool {

    private int startX;
    private int startY;

    private int previousX;
    private int previousY;

    private SpecialText startHighlight = new SpecialText(' ', Color.WHITE, new Color(75, 75, 255, 120));

    @Override
    public void onDrawStart(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        startX = col;
        startY = row;
        highlight.editLayer(col, row, startHighlight);
    }

    @Override
    public void onDraw(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        //highlight.editLayer(startX, startY, startHighlight);
        drawRect(highlight, startX, startY, previousX, previousY, null);
        drawRect(highlight, startX, startY, col, row, startHighlight);
        previousX = col;
        previousY = row;
        //System.out.println("[ArtLine] onDraw");
    }

    @Override
    public void onDrawEnd(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        highlight.editLayer(startX, startY, null);
        drawRect(highlight, startX, startY, col, row, null);
        drawRect(layer, startX, startY, col, row, text);
    }

    private void drawRect(Layer layer, int x1, int y1, int x2, int y2, SpecialText text){
        for (int col = x1; col <= x2; col++){
            layer.editLayer(col, y1, text);
            layer.editLayer(col, y2, text);
        }
        for (int row = y1; row <= y2; row++){
            layer.editLayer(x1, row, text);
            layer.editLayer(x2, row, text);
        }
    }
}
