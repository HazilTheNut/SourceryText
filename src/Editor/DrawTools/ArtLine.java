package Editor.DrawTools;

import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;

import java.awt.*;

/**
 * Created by Jared on 2/25/2018.
 */
public class ArtLine extends DrawTool {

    private int startX;
    private int startY;

    private int previousX;
    private int previousY;

    private SpecialText startHighlight = new SpecialText(' ', Color.WHITE, new Color(75, 75, 255, 120));
    private LayerManager lm;

    public ArtLine(LayerManager manager) {lm = manager; }

    @Override
    public void onDrawStart(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        startX = col;
        startY = row;
    }

    @Override
    public void onDraw(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        //highlight.editLayer(startX, startY, startHighlight);
        int camX = (int)lm.getCameraPos().getX();
        int camY = (int)lm.getCameraPos().getY();
        drawLine(highlight, startX + camX, startY + camY, previousX + camX, previousY + camY, null);
        drawLine(highlight, startX + camX, startY + camY, col + camX, row + camY, startHighlight);
        previousX = col;
        previousY = row;
        //System.out.println("[ArtLine] onDraw");
    }

    @Override
    public void onDrawEnd(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        int camX = (int)lm.getCameraPos().getX();
        int camY = (int)lm.getCameraPos().getY();
        drawLine(highlight, startX + camX, startY + camY, col + camX, row + camY, null);
        drawLine(layer, startX, startY, col, row, text);
    }

    private void drawLine(Layer layer, int x1, int y1, int x2, int y2, SpecialText text){
        System.out.println("[ArtLine] Line Start:\n");
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int distance = (int)Math.round(Math.sqrt(Math.pow((x2 - x1),2) + Math.pow((y2 - y1),2)));
        for (int ii = 0; ii <= distance; ii++){
            int col = (int)Math.round(ii * Math.cos(angle)) + x1;
            int row = (int)Math.round(ii * Math.sin(angle)) + y1;
            layer.editLayer(col, row, text);
            System.out.println("[ArtLine] LINE: x" + col + " y" + row);
        }
    }
}
