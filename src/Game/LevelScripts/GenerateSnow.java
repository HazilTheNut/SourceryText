package Game.LevelScripts;

import Data.Coordinate;
import Data.LevelScriptMask;
import Data.SerializationVersion;
import Engine.SpecialText;
import Game.Registries.TagRegistry;
import Game.Tile;

import java.awt.*;

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
        Tile snowTile = new Tile(pos, "Snow", level);
        level.addOverlayTile(snowTile);
        snowTile.addTag(TagRegistry.WET, snowTile);
        snowTile.addTag(TagRegistry.FROZEN, snowTile);
        snowTile.removeTag(TagRegistry.WET);
        snowTile.addTag(TagRegistry.FLAMMABLE, snowTile);
        snowTile.addTag(TagRegistry.FOOTPRINTS, snowTile);
        snowTile.addTag(TagRegistry.SNOW, snowTile);
        level.getOverlayTileLayer().editLayer(pos.getX(), pos.getY(), new SpecialText(' ', Color.WHITE, new Color(149, 149, 161)));
    }
}
