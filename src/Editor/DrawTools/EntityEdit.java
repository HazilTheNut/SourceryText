package Editor.DrawTools;

import Editor.EntityEditor;
import Data.LevelData;
import Engine.Layer;
import Engine.SpecialText;
import Data.EntityStruct;

import javax.swing.*;

/**
 * Created by Jared on 3/5/2018.
 */
public class EntityEdit extends DrawTool {

    private LevelData ldata;

    @Override
    public void onActivate(JPanel panel) {
        TOOL_TYPE = TYPE_ENTITY;
    }

    public EntityEdit(LevelData levelData){
        ldata = levelData;
    }

    @Override
    public void onDrawStart(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        EntityStruct struct = ldata.getEntityAt(col, row);
        if (struct != null){
            new EntityEditor(struct);
        }
    }
}
