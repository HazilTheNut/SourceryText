package Editor.DrawTools;

import Engine.Layer;
import Engine.SpecialText;

/**
 * Created by Jared on 2/25/2018.
 */
public class ArtFill extends DrawTool {

    @Override
    public void onDrawStart(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        spreadText(layer, col, row, layer.getSpecialText(col, row), text);
    }

    private void spreadText (Layer layer, int col, int row, SpecialText fillOn, SpecialText fillWith){
        if (col < 0 || col >= layer.getCols() || row < 0 || row >= layer.getCols()) return;
        if (fillWith == null) return;
        if (fillOn != null && fillOn.equals(fillWith)) return;
        SpecialText txtAtLoc = layer.getSpecialText(col,row);
        if ((txtAtLoc == null && fillOn == null) || (txtAtLoc != null && txtAtLoc.equals(fillOn))){
            layer.editLayer(col, row, fillWith);
            spreadText(layer, col+1, row, fillOn, fillWith);
            spreadText(layer, col-1, row, fillOn, fillWith);
            spreadText(layer, col, row+1, fillOn, fillWith);
            spreadText(layer, col, row-1, fillOn, fillWith);
        }
    }
}
