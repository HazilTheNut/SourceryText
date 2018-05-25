package Editor.DrawTools;

import Data.LevelData;
import Data.WarpZone;
import Editor.*;
import Data.FileIO;
import Engine.Layer;
import Engine.SpecialText;

import javax.swing.*;
import java.awt.*;
import java.io.*;

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
        if (selectedWarpZone != null) {
            FileIO io = new FileIO();
            File levelFile;
            if (selectedWarpZone.getRoomFilePath().equals(""))
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
                    new WarpZoneEditor(nextLevel, selectedWarpZone);
                }
            }
        }
    }
}
