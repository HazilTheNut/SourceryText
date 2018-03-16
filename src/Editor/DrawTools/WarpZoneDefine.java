package Editor.DrawTools;

import Editor.LevelData;
import Editor.WarpZone;
import Editor.WarpZoneEditor;
import Engine.Layer;
import Engine.SpecialText;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Jared on 2/25/2018.
 */
public class WarpZoneDefine extends DrawTool {

    private int startX;
    private int startY;

    private int previousX;
    private int previousY;

    private SpecialText previewHighlight = new SpecialText(' ', Color.WHITE, new Color(255, 0, 155, 120));

    private LevelData ldata;

    public WarpZoneDefine(LevelData levelData) {
        ldata = levelData;
    }

    @Override
    public void onActivate(JPanel panel) {
        TOOL_TYPE = TYPE_TILE;
    }

    @Override
    public void onDrawEnd(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        WarpZone selectedWarpZone = ldata.getSelectedWarpZone();
        new WarpZoneEditor(ldata, selectedWarpZone);
    }
}
