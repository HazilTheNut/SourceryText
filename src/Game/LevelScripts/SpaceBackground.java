package Game.LevelScripts;

import Data.LayerImportances;
import Data.SerializationVersion;
import Engine.Layer;
import Engine.SpecialText;

import java.util.Random;

public class SpaceBackground extends LevelScript {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private Layer backgroundLayer;

    @Override
    public void onLevelLoad() {
        drawBackground();
    }

    @Override
    public void onLevelEnter() {
        gi.getLayerManager().addLayer(backgroundLayer);
    }

    @Override
    public void onLevelExit() {
        gi.getLayerManager().removeLayer(backgroundLayer);
    }

    @Override
    public void onAnimatedTileUpdate() {
        backgroundLayer.setPos(gi.getLayerManager().getCameraPos());
    }

    private void drawBackground(){
        int STARCOUNTER_START = 20; //Ensures minimum of 10 stars per tile
        int starCounter = STARCOUNTER_START;
        float starLikelihood = 1f / STARCOUNTER_START;
        backgroundLayer = new Layer(gi.getLayerManager().getWindow().RESOLUTION_WIDTH, gi.getLayerManager().getWindow().RESOLUTION_HEIGHT, "space_background: " + level.getName(), 0, 0, LayerImportances.BACKDROP - 1);
        Random random = new Random();
        for (int col = 0; col < level.getBackdrop().getCols(); col++) {
            for (int row = 0; row < level.getBackdrop().getRows(); row++) {
                starCounter--;
                if (starCounter == 0 || random.nextFloat() < starLikelihood){
                    backgroundLayer.editLayer(col, row, new SpecialText('.'));
                    starCounter = STARCOUNTER_START;
                }
            }
        }
    }
}
