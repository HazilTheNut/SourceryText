package Editor.ArtTools;

import Engine.Layer;
import Engine.SpecialText;

/**
 * Created by Jared on 2/25/2018.
 */
public class ArtEraser extends ArtBrush {

    public ArtEraser() {name = "Eraser"; }

    @Override
    public void onDraw(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        drawBrush(layer, col, row, null);
    }

    @Override
    public void onDrawStart(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        drawBrush(layer, col, row, null);
    }
}
