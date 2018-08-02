package Game.LevelScripts;

import Data.Coordinate;
import Data.SerializationVersion;
import Game.OverlayTileGenerator;

public class GenerateSnow extends LevelScript {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public String[] getMaskNames() {
        return new String[]{"snow", "ice"};
    }

    @Override
    public void onLevelLoad() {
        OverlayTileGenerator otg = new OverlayTileGenerator();
        for (int col = 0; col < level.getWidth(); col++) {
            for (int row = 0; row < level.getHeight(); row++) {
                Coordinate loc = new Coordinate(col, row);
                if (getMaskDataAt("snow", loc))
                    otg.createSnowTile(loc, level);
                else if (getMaskDataAt("ice", loc))
                    otg.createIceTile(loc, level);
            }
        }
    }
}
