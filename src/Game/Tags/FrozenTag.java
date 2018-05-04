package Game.Tags;

import Engine.SpecialText;
import Game.Entities.Entity;
import Game.Registries.TagRegistry;
import Game.TagEvent;
import Game.Tile;

import java.awt.*;

/**
 * Created by Jared on 4/25/2018.
 */
public class FrozenTag extends Tag {

    private int duration = 4;

    @Override
    public void onAddThis(TagEvent e) {
        if (e.getTarget() instanceof Tile && !e.getTarget().hasTag(TagRegistry.WET)){
            e.cancel();
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

    @Override
    public void onAdd(TagEvent e) {
        if (e.getTarget().hasTag(TagRegistry.ON_FIRE)){
            e.addCancelableAction(event -> e.getTarget().removeTag(getId()));
        }
    }

    @Override
    public Color getTagColor() {
        return new Color(145, 150, 199);
    }

    @Override
    public void onEntityAction(TagEvent e) {
        e.cancel();
    }

    @Override
    public void onTurn(TagEvent e) {
        if (e.getTarget() instanceof Entity){
            duration--;
            if (duration <= 0){
                e.addCancelableAction(event -> e.getTarget().removeTag(getId()));
            }
        }
    }
}
