package Game.LevelScripts;

import Data.Coordinate;
import Data.LevelScriptMask;
import Data.SerializationVersion;
import Game.OverlayTileGenerator;

public class GenerateSnow extends LevelScript {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public String[] getMaskNames() {
        return new String[]{"snow"};
    }

    @Override
    public void onLevelLoad() {
        LevelScriptMask snowMask = getMask("snow");
        for (int col = 0; col < snowMask.getMask().length; col++) {
            for (int row = 0; row < snowMask.getMask()[0].length; row++) {
                if (snowMask.getMask()[col][row])
                    generateSnow(new Coordinate(col, row));
            }
        }
    }

    public void generateSnow(Coordinate pos){
        OverlayTileGenerator tg = new OverlayTileGenerator();
        tg.createSnowTile(pos, level);
    }
}
