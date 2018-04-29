package Editor.DrawTools;

import Engine.Layer;
import Engine.SpecialText;

/**
 * Created by Jared on 2/25/2018.
 */
public class ArtEraser extends ArtBrush {
    /**
     * ArtEraser:
     *
     * Inherits the ArtBrush class, but instead using null SpecialText's instead of input SpecialText.
     */

    public ArtEraser() { name = "Eraser Tool"; }

    @Override
    public void onDraw(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        drawBrush(layer, col, row, null);
    }

    @Override
    public void onDrawStart(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        drawBrush(layer, col, row, null);
    }
}
