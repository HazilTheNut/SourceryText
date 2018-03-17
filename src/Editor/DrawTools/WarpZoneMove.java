package Editor.DrawTools;

import Data.LevelData;
import Data.WarpZone;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Jared on 3/5/2018.
 */
public class WarpZoneMove extends ArtRectangle {

    private LevelData ldata;
    private WarpZone toMove;
    private int moveFromPosX = 0;
    private int moveFromPosY = 0;

    private int prevX = 0;
    private int prevY = 0;
    private int displayXOffset = 0;
    private int displayYOffset = 0;

    private boolean movingZone = false;

    @Override
    public void onActivate(JPanel panel) {
        TOOL_TYPE = TYPE_TILE;
    }

    public WarpZoneMove(LayerManager lm, LevelData levelData){
        super(lm);
        ldata = levelData;
    }

    @Override
    public void onDrawStart(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        toMove = ldata.getSelectedWarpZone();
        if (toMove != null) {
            moveFromPosX = col;
            moveFromPosY = row;
            prevX = col;
            prevY = row;
            displayXOffset = toMove.getXpos() - col - (int) lm.getCameraPos().getX();
            displayYOffset = toMove.getYpos() - row - (int) lm.getCameraPos().getY();
            highlightDraggedRect(highlight, col, row, new SpecialText(' ', Color.WHITE, new Color(125, 125, 250, 75)));
            movingZone = true;
        }
    }

    @Override
    public void onDraw(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        if (movingZone) {
            highlightDraggedRect(highlight, prevX, prevY, null);
            highlightDraggedRect(highlight, col, row, new SpecialText(' ', Color.WHITE, new Color(125, 125, 250, 75)));
            prevX = col;
            prevY = row;
        }
    }

    @Override
    public void onDrawEnd(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        if (movingZone) {
            toMove.setPos(toMove.getXpos() + (col - moveFromPosX), toMove.getYpos() + (row - moveFromPosY));
            highlightDraggedRect(highlight, col, row, null);
            ldata.updateWarpZoneLayer(col, row);
        }
        movingZone = false;
    }

    private void highlightDraggedRect(Layer highlight, int col, int row, SpecialText text){
        drawRect(highlight, col + displayXOffset, row + displayYOffset, col + displayXOffset + toMove.getWidth() - 1, row + displayYOffset + toMove.getHeight() - 1, text, true);
    }
}
