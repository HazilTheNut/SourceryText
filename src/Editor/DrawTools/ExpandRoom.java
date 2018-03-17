package Editor.DrawTools;

import Editor.LevelData;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;

import javax.swing.*;

/**
 * Created by Jared on 3/4/2018.
 */
public class ExpandRoom extends DrawTool {

    private LevelData ldata;
    private LayerManager lm;

    public ExpandRoom(LevelData levelData, LayerManager manager){
        ldata = levelData;
        lm = manager;
    }

    @Override
    public void onActivate(JPanel panel) {
        TOOL_TYPE = TYPE_TILE;
    }

    @Override
    public void onDrawStart(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        System.out.println("Draw pos: " + col + "," + row);
        System.out.println("Layer dim: " + layer.getCols() + "x"  + layer.getRows());
        ldata.resize(col, row);
        if (col < 0)
            lm.moveCameraPos(-col, 0);
        if (row < 0)
            lm.moveCameraPos(0, -row);
    }
}
