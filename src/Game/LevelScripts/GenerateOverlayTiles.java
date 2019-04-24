package Game.LevelScripts;

import Data.Coordinate;
import Data.SerializationVersion;
import Game.OverlayTileGenerator;
import Game.Registries.TagRegistry;
import Game.Tile;

public class GenerateOverlayTiles extends LevelScript {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public String[] getMaskNames() {
        return new String[]{"snow", "ice", "bridge", "ash", "fire", "fireplace"};
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
                else if (getMaskDataAt("bridge", loc))
                    otg.createBridgeTile(loc, level);
                else if (getMaskDataAt("ash", loc))
                    otg.createAshTile(loc, level);
                else if (getMaskDataAt("fire", loc)){
                    createFireTile(loc, otg);
                } else if (getMaskDataAt("fireplace", loc)){
                    Tile tile = createFireTile(loc, otg);
                    tile.addTag(TagRegistry.BURN_NOSPREAD, tile);
                    tile.addTag(TagRegistry.TILE_WALL, tile);
                }
            }
        }
    }

    private Tile createFireTile(Coordinate loc, OverlayTileGenerator otg){
        Tile tile = otg.createAshTile(loc, level);
        tile.addTag(TagRegistry.FLAMMABLE, tile);
        tile.addTag(TagRegistry.BURN_FOREVER, tile);
        tile.addTag(TagRegistry.ON_FIRE, tile);
        tile.removeTag(TagRegistry.FLAMMABLE);
        return tile;
    }
}