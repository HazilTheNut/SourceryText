package Editor.ArtTools;

import Engine.Layer;
import Engine.SpecialText;

/**
 * Created by Jared on 2/25/2018.
 */
public class ArtPencil extends ArtTool {

    @Override
    public void onDraw(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        layer.editLayer(col, row, text);
    }

    @Override
    public void onDrawStart(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        layer.editLayer(col, row, text);
    }
}
