package Editor.DrawTools;

import Engine.Layer;
import Engine.LayerManager;
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

    public ArtEraser(LayerManager lm) {
        super(lm);
        name = "Eraser Tool";
    }

    @Override
    void drawBrush(Layer layer, int centerCol, int centerRow, SpecialText text) {
        super.drawBrush(layer, centerCol, centerRow, null);
    }
}
