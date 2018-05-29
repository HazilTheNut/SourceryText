package Editor.DrawTools;

import Engine.Layer;
import Engine.SpecialText;

import javax.swing.*;

/**
 * Created by Jared on 2/25/2018.
 */
public abstract class DrawTool {

    /**
     * The basic tool for anything that has to do with editing the LevelData at a certain position.
     * So the backdrop, tile data, entities, and warp zones.
     */

    public int TOOL_TYPE = 0;
    public static final int TYPE_ART = 1;    //Used to mark which camera view to switch to.
    public static final int TYPE_TILE = 2;
    public static final int TYPE_ENTITY = 3;

    //Ran upon pressing left click down
    public void onDrawStart(Layer layer, Layer highlight, int col, int row, SpecialText text) {}

    //Ran while left click is pressed
    public void onDraw(Layer layer, Layer highlight, int col, int row, SpecialText text) {}

    //Ran upon releasing left click
    public void onDrawEnd(Layer layer, Layer highlight, int col, int row, SpecialText text) {}

    //Ran when switching to new tool
    public void onDeactivate(JPanel panel) {}

    //Ran when selected as the tool
    public void onActivate(JPanel panel) {}

    //Ran when right-click during drawing
    public void onCancel(Layer highlight, int col, int row){}
}
