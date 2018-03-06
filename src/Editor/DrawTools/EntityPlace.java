package Editor.DrawTools;

import Editor.LevelData;
import Engine.Layer;
import Engine.SpecialText;
import Game.Registries.EntityRegistry;

import javax.swing.*;

/**
 * Created by Jared on 3/5/2018.
 */
public class EntityPlace extends DrawTool {

    private LevelData ldata;
    private int id;
    private EntityRegistry entityRegistry;

    @Override
    public void onActivate(JPanel panel) {
        TOOL_TYPE = TYPE_ENTITY;
    }

    public EntityPlace(LevelData levelData){
        ldata = levelData;
    }

    public void setEntityStruct(int id) { this.id = id; }

    @Override
    public void onDrawStart(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        ldata.getEntityLayer().editLayer(col, row, entityRegistry.getEntityStruct(id).getDisplayChar());
        ldata.setTileData(col, row, id);
    }
}
