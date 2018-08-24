package Editor.DrawTools;

import Data.Coordinate;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;

/**
 * Created by Jared on 2/25/2018.
 */
public class ArtBrush extends ArtLine {

    /**
     * ArtBrush:
     *
     * The 'basic' art tool.
     *
     * It inherits the ArtLine tool in order to handle fast mouse movements. It uses ArtLine's line-drawing method to fill in any gaps.
     */

    public ArtBrush(LayerManager manager) {
        super(manager);
        name = "Brush Tool";
        label = "Size: ";
    }

    private Coordinate prevPos;

    @Override
    public void onDraw(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        drawLine(layer, col, row, prevPos.getX(), prevPos.getY(), text);
        prevPos = new Coordinate(col, row);
    }

    @Override
    public void onDrawStart(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        prevPos = new Coordinate(col, row);
        drawBrush(layer, col, row, text);
    }

    @Override
    public void onDrawEnd(Layer layer, Layer highlight, int col, int row, SpecialText text) { }
}
