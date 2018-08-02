package Game.Tags;

import Data.SerializationVersion;
import Engine.SpecialText;
import Game.GameInstance;
import Game.LevelScripts.GenerateSnow;
import Game.Registries.TagRegistry;
import Game.TagEvent;
import Game.Tags.TempTags.OnFireTag;
import Game.Tile;

import java.awt.*;

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
            if (tile.hasTag(TagRegistry.ASH)){
                OnFireTag onFireTag = new OnFireTag();
                Tile ashTile = onFireTag.createAshTile(tile.getLocation(), gi.getCurrentLevel());
                gi.getCurrentLevel().addOverlayTile(ashTile);
            } else if (tile.hasTag(TagRegistry.SNOW)){
                GenerateSnow snowGenerator = new GenerateSnow();
                snowGenerator.initialize(gi, gi.getCurrentLevel());
                snowGenerator.generateSnow(tile.getLocation());
            } else {
                if (gi.getCurrentLevel().getOverlayTileAt(tile.getLocation()) != null)
                    gi.getCurrentLevel().removeOverlayTile(tile);
                else {
                    Tile sandTile = new Tile(tile.getLocation(), "Sand", gi.getCurrentLevel());
                    sandTile.addTag(TagRegistry.SAND, sandTile);
                    sandTile.addTag(TagRegistry.FOOTPRINTS, sandTile);
                    gi.getCurrentLevel().addOverlayTile(sandTile);
                    gi.getCurrentLevel().getOverlayTileLayer().editLayer(sandTile.getLocation(), new SpecialText(' ', Color.WHITE, new Color(189, 182, 153)));
                }
            }
        }
    }
}
