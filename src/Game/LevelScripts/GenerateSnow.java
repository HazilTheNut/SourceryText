package Game.LevelScripts;

import Data.Coordinate;
import Data.LevelScriptMask;
import Engine.SpecialText;
import Game.Registries.TagRegistry;
import Game.Tile;

import java.awt.*;

public class GenerateSnow extends LevelScript {

    @Override
    public String[] getMaskNames() {
        return new String[]{"snow"};
    }

    @Override
    public void onLevelLoad() {
        LevelScriptMask snowMask = getMask("snow");
        for (int col = 0; col < snowMask.getMask().length; col++) {
            for (int row = 0; row < snowMask.getMask()[0].length; row++) {
                if (snowMask.getMask()[col][row]){
                    Tile snowTile = new Tile(new Coordinate(col, row), "Snow", level);
                    level.addOverlayTile(snowTile);
                    snowTile.addTag(TagRegistry.WET, snowTile);
                    snowTile.addTag(TagRegistry.FROZEN, snowTile);
                    snowTile.removeTag(TagRegistry.WET);
                    snowTile.addTag(TagRegistry.FLAMMABLE, snowTile);
                    level.getOverlayTileLayer().editLayer(col, row, new SpecialText(' ', Color.WHITE, new Color(180, 180, 204)));
                }
            }
        }
    }
}
