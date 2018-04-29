package Game.Tags;

import Engine.SpecialText;
import Game.Registries.TagRegistry;
import Game.TagEvent;
import Game.Tile;

import java.awt.*;

/**
 * Created by Jared on 4/25/2018.
 */
public class FrozenTag extends Tag {

    @Override
    public void onAddThis(TagEvent e) {
        if (!e.getTarget().hasTag(TagRegistry.WET)){
            e.cancel();
            //DebugWindow.reportf(DebugWindow.TAGS, "[FrozenTag] Target is wet!");
        } else {
            //DebugWindow.reportf(DebugWindow.TAGS, "[FrozenTag] Target is not wet!");
        }
        e.addCancelableAction(event -> {
            if (e.getTarget() instanceof Tile) {
                Tile target = (Tile) e.getTarget();
                Tile overlay = target.getLevel().getOverlayTileAt(target.getLocation());
                if (overlay == null){
                    Tile iceTile = new Tile(target.getLocation(), "Ice", target.getLevel());
                    target.getLevel().addOverlayTile(iceTile);
                    iceTile.addTag(TagRegistry.WET, e.getSource());
                    iceTile.addTag(TagRegistry.FROZEN, e.getSource());
                    iceTile.removeTag(TagRegistry.WET);
                    SpecialText iceText;
                    if (e.getTarget().hasTag(TagRegistry.TILE_WALL)) {
                        iceTile.addTag(TagRegistry.TILE_WALL, e.getSource());
                        iceText = new SpecialText(' ', Color.WHITE, new Color(147, 166, 199));
                    } else {
                        iceText = new SpecialText(' ', Color.WHITE, new Color(109, 133, 166));
                    }
                    target.getLevel().getOverlayTileLayer().editLayer(iceTile.getLocation(), iceText);
                }
            }
        });
    }
}
