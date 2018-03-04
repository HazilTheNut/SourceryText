package Editor.DrawTools;

import Engine.Layer;
import Engine.SpecialText;

import javax.swing.*;

/**
 * Created by Jared on 2/25/2018.
 */
public abstract class DrawTool {



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
}
