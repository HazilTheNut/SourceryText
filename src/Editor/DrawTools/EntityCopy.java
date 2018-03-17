package Editor.DrawTools;

import Data.LevelData;
import Engine.Layer;
import Engine.SpecialText;
import Data.EntityStruct;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Jared on 3/5/2018.
 */
public class EntityCopy extends DrawTool {

    private LevelData ldata;
    private EntityStruct toCopy;
    private int copyFromPosX = 0;
    private int copyFromPosY = 0;


    @Override
    public void onActivate(JPanel panel) {
        TOOL_TYPE = TYPE_ENTITY;
    }

    public EntityCopy(LevelData levelData){
        ldata = levelData;
    }

    @Override
    public void onDrawStart(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        toCopy = ldata.getEntityAt(col, row);
        copyFromPosX = col;
        copyFromPosY = row;
        highlight.editLayer(col, row, new SpecialText(' ', Color.WHITE, new Color(255, 125, 0, 50)));
    }

    @Override
    public void onDraw(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        highlight.editLayer(copyFromPosX, copyFromPosY, new SpecialText(' ', Color.WHITE, new Color(255, 125, 0, 50)));
    }

    @Override
    public void onDrawEnd(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        ldata.setEntityData(col, row, toCopy.copy());
        highlight.editLayer(copyFromPosX, copyFromPosY, null);
    }
}
