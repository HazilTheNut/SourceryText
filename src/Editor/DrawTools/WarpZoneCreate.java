package Editor.DrawTools;

import Editor.LevelData;
import Editor.WarpZone;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Jared on 2/25/2018.
 */
public class WarpZoneCreate extends ArtRectangle {

    private int startX;
    private int startY;

    private int previousX;
    private int previousY;

    private SpecialText previewHighlight = new SpecialText(' ', Color.WHITE, new Color(255, 0, 155, 120));

    private LevelData ldata;

    public WarpZoneCreate(LayerManager manager, LevelData levelData) {
        super(manager);
        ldata = levelData;
    }

    @Override
    public void onActivate(JPanel panel) {
        TOOL_TYPE = TYPE_TILE;
    }

    @Override
    public void onDrawStart(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        startX = col;
        startY = row;
    }

    @Override
    public void onDraw(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        int xOffset = (int)lm.getCameraPos().getX() + layer.getX();
        int yOffset = (int)lm.getCameraPos().getY() + layer.getY();
        drawRect(highlight, startX + xOffset, startY + yOffset, previousX + xOffset, previousY + yOffset, null, true);
        drawRect(highlight, startX + xOffset, startY + yOffset, col + xOffset, row + yOffset, previewHighlight, true);
        previousX = col;
        previousY = row;
    }

    @Override
    public void onDrawEnd(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        int xOffset = (int)lm.getCameraPos().getX() + layer.getX();
        int yOffset = (int)lm.getCameraPos().getY() + layer.getY();
        drawRect(highlight, startX + xOffset, startY + yOffset, col + xOffset, row + yOffset, null, true);
        WarpZone warpZone = new WarpZone(startX, startY, col - startX + 1, row - startY + 1);
        System.out.println(String.format("x %1$d y %2$d w %3$d h %4$d", startX, startY, col - startX, row - startY));
        ldata.addWarpZone(warpZone);
        ldata.updateWarpZoneLayer(col, row);
    }
}
