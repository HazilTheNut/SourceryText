package Game.LevelScripts;

import Data.SerializationVersion;
import Game.Tile;

public class VillageDetectFires extends LevelScript {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public String[] getMaskNames() {
        return new String[]{"Houses"};
    }

    @Override
    public void onAddOverlayTile(Tile tile) {
        if (tile.getName().equals("Ash") && getMaskDataAt("Houses", tile.getLocation())){
            gi.getFactionManager().getFaction("villager").addOpinion("player", -4);
        }
    }
}
