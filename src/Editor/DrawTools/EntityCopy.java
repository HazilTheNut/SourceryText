package Editor.DrawTools;

import Data.LevelData;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Data.EntityStruct;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Jared on 3/5/2018.
 */
public class EntityCopy extends DrawTool {

    /**
     * EntityCopy
     *
     * The Tool that copies entities. Wow.
     */

    private LevelData ldata;
    private EntityStruct toCopy; //Gotta store the selected entity in memory to copy when you release the mouse button
    private int copyFromPosX = 0;
    private int copyFromPosY = 0;

    private LayerManager lm;

    @Override
    public void onActivate(JPanel panel) {
        TOOL_TYPE = TYPE_ENTITY;
    }

    public EntityCopy(LayerManager manager, LevelData levelData){
        ldata = levelData;
        lm = manager;
    }

    @Override
    public void onDrawStart(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        toCopy = ldata.getEntityAt(col, row); //Get the entity
        copyFromPosX = col; //Also needs to remember where the copying entity is so that it the highlight can be consistently displayed
        copyFromPosY = row;
        highlight.editLayer(col - lm.getCameraPos().getX(), row - lm.getCameraPos().getY(), new SpecialText(' ', Color.WHITE, new Color(255, 125, 0, 50)));
    }

    @Override
    public void onDraw(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        highlight.editLayer(copyFromPosX - lm.getCameraPos().getX(), copyFromPosY - lm.getCameraPos().getY(), new SpecialText(' ', Color.WHITE, new Color(255, 125, 0, 50)));
    }

    @Override
    public void onDrawEnd(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        if (toCopy != null)
            ldata.setEntityData(col, row, toCopy.copy()); //It...copies an entity? Interesting.
        highlight.clearLayer();
    }

    @Override
    public void onCancel(Layer highlight, int col, int row) {
        highlight.clearLayer();
    }
}
