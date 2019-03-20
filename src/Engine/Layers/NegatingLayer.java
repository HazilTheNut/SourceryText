package Engine.Layers;

import Data.Coordinate;
import Data.SerializationVersion;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;

import java.awt.*;

public class NegatingLayer extends Layer {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    public NegatingLayer(int w, int h, String layerName, int x, int y, int priority) {
        super(w, h, layerName, x, y, priority);
    }

    @Override
    public SpecialText provideTextForDisplay(LayerManager lm, Coordinate layerPos, Coordinate screenPos, int position) {
        SpecialText beneath = lm.projectSpecialTextToScreen(screenPos.getX(), screenPos.getY(), position - 1);
        Color rawFg = beneath.getFgColor();
        Color rawBg = beneath.getBkgColor();
        return new SpecialText ( //This formatting is totally non-standard, but in this case I think it makes it easier to read.
                beneath.getCharacter(),
                new Color(255 - rawFg.getRed(), 255 - rawFg.getGreen(), 255 - rawFg.getBlue()),
                new Color(255 - rawBg.getRed(), 255 - rawBg.getGreen(), 255 - rawBg.getBlue())
        );
    }
}
