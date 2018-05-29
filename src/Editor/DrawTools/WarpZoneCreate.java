package Editor.DrawTools;

import Data.LevelData;
import Data.WarpZone;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Jared on 2/25/2018.
 */
public class WarpZoneCreate extends ArtRectangle {

    /**
     * WarpZoneCreate:
     *
     * Inheritor of ArtRectangle, because code re-use is beautiful. The rectangle fill function is too useful to not use.
     * Lo and behold, WarpZoneCreate... creates Warp Zones.
     */

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
        int xOffset = -lm.getCameraPos().getX() + layer.getX();
        int yOffset = -lm.getCameraPos().getY() + layer.getY();
        drawRect(highlight, startX + xOffset, startY + yOffset, previousX + xOffset, previousY + yOffset, null, true); //I usually try to make code fixed width to make it easier to read.
        drawRect(highlight, startX + xOffset, startY + yOffset, col + xOffset, row + yOffset, previewHighlight, true); //In most cases, I just add more spaces. But I dn't have to here. It gives me a warm, fuzzy feeling inside.
        previousX = col;
        previousY = row;
    }

    @Override
    public void onDrawEnd(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        highlight.clearLayer();
        int cornerX = Math.min(startX, col); //Much like ArtRectangle, we don't know if the ending point is ahead or behind the start point.
        int cornerY = Math.min(startY, row);
        int width   = Math.max(col - startX + 1, startX - col + 1);
        int height  = Math.max(row - startY + 1, startY - row + 1);
        WarpZone warpZone = new WarpZone(cornerX, cornerY, width, height);
        System.out.println(String.format("x %1$d y %2$d w %3$d h %4$d", startX, startY, col - startX, row - startY));
        ldata.addWarpZone(warpZone);
        ldata.updateWarpZoneLayer(col, row);
    }

    @Override
    public void onCancel(Layer highlight, int col, int row) {
        highlight.clearLayer();
    }
}
