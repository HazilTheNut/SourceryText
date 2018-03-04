package Editor.DrawTools;

import Engine.Layer;
import Engine.SpecialText;

import javax.swing.*;

/**
 * Created by Jared on 2/25/2018.
 */
public class ArtEraser extends ArtBrush {

    public ArtEraser() {name = "Eraser Tool"; }

    @Override
    public void onActivate(JPanel panel) {
        TOOL_TYPE = DrawTool.TYPE_ART;
    }

    @Override
    public void onDraw(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        drawBrush(layer, col, row, null);
    }

    @Override
    public void onDrawStart(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        drawBrush(layer, col, row, null);
    }
}
