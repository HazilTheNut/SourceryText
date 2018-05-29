package Editor.DrawTools;

import Data.FileIO;
import Data.LevelData;
import Data.WarpZone;
import Editor.WarpZoneEditor;
import Engine.Layer;
import Engine.SpecialText;

import javax.swing.*;
import java.io.File;

/**
 * Created by Jared on 2/25/2018.
 */
public class WarpZoneDefine extends DrawTool {

    /**
     * WarpZoneDefine:
     *
     * The Tool that directs the output of a Warp Zone. Specifically, where and which level.
     * If you change the file location of the level, the WarpZone WILL NOT update accordingly.
     */

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
        if (selectedWarpZone != null) {
            FileIO io = new FileIO(); //FileIO is such a wonderful utility. I am more than glad that I made it.
            File levelFile;
            if (selectedWarpZone.getRoomFilePath().equals("")) //If not defined yet, goes to default starting path (the 'root' according to FileIO)
                levelFile = io.chooseLevelData();
            else
                levelFile = io.chooseLevelData(io.getRootFilePath() + selectedWarpZone.getRoomFilePath());
            if (levelFile != null) {
                LevelData nextLevel = io.openLevel(levelFile);
                if (nextLevel != null) {
                    String levelPath = io.decodeFilePath(levelFile.getPath());
                    System.out.println("[WarpZoneDefine] full file path: " + levelPath);
                    System.out.println("[WarpZoneDefine] relative file path: " + io.getRelativeFilePath(levelPath));
                    selectedWarpZone.setRoomFilePath(io.getRelativeFilePath(levelPath));
                    new WarpZoneEditor(nextLevel, selectedWarpZone); //Here is where the meat of the editing happens.
                }
            }
        }
    }
}
