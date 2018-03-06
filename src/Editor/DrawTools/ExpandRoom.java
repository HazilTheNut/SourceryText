package Editor.DrawTools;

import Editor.LevelData;
import Engine.Layer;
import Engine.SpecialText;

import javax.swing.*;

/**
 * Created by Jared on 3/4/2018.
 */
public class ExpandRoom extends DrawTool {

    LevelData ldata;

    public ExpandRoom(LevelData levelData){
        ldata = levelData;
    }

    @Override
    public void onActivate(JPanel panel) {
        TOOL_TYPE = TYPE_TILE;
    }

    @Override
    public void onDrawStart(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        ldata.resize(col, row);
        System.out.println("Draw pos: " + col + "," + row);
    }
}
