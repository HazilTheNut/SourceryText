package Game.Tags;

import Data.SerializationVersion;
import Game.GameInstance;
import Game.OverlayTileGenerator;
import Game.Registries.TagRegistry;
import Game.TagEvent;
import Game.Tile;

public class DiggingTag extends Tag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onContact(TagEvent e) {
        if (e.getTarget() instanceof Tile) {
            Tile target = (Tile) e.getTarget();
            dig(target, e.getGameInstance());
        }
    }

    public void dig(Tile tile, GameInstance gi){
        if (tile.hasTag(TagRegistry.DIGGABLE)){
            OverlayTileGenerator otg = new OverlayTileGenerator();
            if (tile.hasTag(TagRegistry.ASH)){
                otg.createAshTile(tile.getLocation(), tile.getLevel());
            } else if (tile.hasTag(TagRegistry.SNOW)){
                otg.createSnowTile(tile.getLocation(), tile.getLevel());
            } else {
                if (gi.getCurrentLevel().getOverlayTileAt(tile.getLocation()) != null && gi.getCurrentLevel().getBaseTileAt(tile.getLocation()).hasTag(TagRegistry.FOOTPRINTS))
                    gi.getCurrentLevel().removeOverlayTile(tile);
                else {
                    otg.createSandTile(tile.getLocation(), tile.getLevel());
                }
            }
        }
    }
}
