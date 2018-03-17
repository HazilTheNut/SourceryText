package Editor.DrawTools;

import Editor.*;
import Engine.FileIO;
import Engine.Layer;
import Engine.SpecialText;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URLDecoder;

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
        FileIO io = new FileIO();
        File levelFile = io.chooseLevel();
        LevelData nextLevel = io.openLevel(levelFile);
        if (nextLevel != null && selectedWarpZone != null) {
            selectedWarpZone.setRoomFilePath(levelFile.getPath());
            new WarpZoneEditor(nextLevel, selectedWarpZone);
        } else {
            JOptionPane.showMessageDialog(new JFrame(), "ERROR: Warp Zone not selected or \nfile being accessed is out of date / improper!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
