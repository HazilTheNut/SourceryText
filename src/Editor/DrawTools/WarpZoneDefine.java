package Editor.DrawTools;

import Editor.*;
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
        File levelFile = chooseLevel();
        LevelData nextLevel = openLevel(levelFile);
        if (nextLevel != null && selectedWarpZone != null) {
            selectedWarpZone.setRoomFilePath(levelFile.getPath());
            new WarpZoneEditor(nextLevel, selectedWarpZone);
        }
    }


    //Copied from EditorToolPanel
    //TODO Make a file input / output object

    private File chooseLevel(){
        String path;
        String decodedPath = "";
        try {
            path = EditorToolPanel.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            decodedPath = URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException | URISyntaxException e) {
            e.printStackTrace();
        }
        JFileChooser chooser = new JFileChooser(decodedPath);
        System.out.println(decodedPath);
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Sourcery Text Level Data", "lda");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(new Component() {
        });
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            System.out.println("You chose to open this file: " +
                    chooser.getSelectedFile().getName());
            return chooser.getSelectedFile();
        } else return null;
    }

    private LevelData openLevel( File savedLevel ){
        try {
            FileInputStream fileIn = new FileInputStream(savedLevel);
            ObjectInputStream objIn = new ObjectInputStream(fileIn);
            return (LevelData)objIn.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
