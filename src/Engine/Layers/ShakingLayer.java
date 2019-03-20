package Engine.Layers;

import Data.Coordinate;
import Data.SerializationVersion;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;

public class ShakingLayer extends Layer {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    int[] shakeAnimation;
    float currentFrame = 0;

    public ShakingLayer(int w, int h, String layerName, int x, int y, int priority) {
        super(w, h, layerName, x, y, priority);
        shakeAnimation = new int[]{0, -1, 0, 1};
    }

    @Override
    public SpecialText provideTextForDisplay(LayerManager lm, Coordinate layerPos, Coordinate screenPos, int position) {
        currentFrame += 0.04;
        if (currentFrame >= shakeAnimation.length) currentFrame = 0;
        int multiplier = (screenPos.getY() % 2 == 0) ? -1 : 1; //The shaking should zig-zag between display rows.
        return lm.projectSpecialTextToScreen(screenPos.getX() + (shakeAnimation[(int)currentFrame] * multiplier), screenPos.getY(), position - 1);
    }
}
