package Game.Tags;

import Data.SerializationVersion;
import Engine.SpecialText;
import Game.Debug.DebugWindow;
import Game.Entities.Entity;
import Game.Registries.TagRegistry;
import Game.TagEvent;
import Game.TagHolder;
import Game.Tile;

import java.awt.*;

/**
 * Created by Jared on 4/25/2018.
 */
public class FrozenTag extends Tag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private int duration = 4;

    @Override
    public void onAddThis(TagEvent e) {
        if (e.getTarget() instanceof Tile) {
            if (e.getTarget().hasTag(TagRegistry.WET)) {
                Tile target = (Tile) e.getTarget();
                Tile overlay = target.getLevel().getOverlayTileAt(target.getLocation());
                if (overlay == null) {
                    e.cancel();
                    createIceOverlayTile(target, e.getSource());
                    e.addFutureAction(event -> target.removeTag(TagRegistry.FROZEN));
                }
            } else
                e.cancel();
        }
    }

    private void createIceOverlayTile(Tile target, TagHolder source){
        Tile iceTile = new Tile(target.getLocation(), "Ice", target.getLevel());
        target.getLevel().addOverlayTile(iceTile);
        iceTile.addTag(TagRegistry.WET, source);
        iceTile.addTag(TagRegistry.FROZEN, source);
        iceTile.removeTag(TagRegistry.WET);
        iceTile.addTag(TagRegistry.FLAMMABLE, source);
        SpecialText iceText;
        int hash = 23 * 113 * iceTile.getLocation().getX() + iceTile.getLocation().getY();
        DebugWindow.reportf(DebugWindow.MISC, "FrozenTag","Ice Hash: %1$d", hash);
        char tileChar = (hash % 7 == 0) ? '/' : ' ';
        if (target.hasTag(TagRegistry.TILE_WALL)) {
            iceTile.addTag(TagRegistry.TILE_WALL, source);
            iceTile.addTag(TagRegistry.NO_PATHING, source);
            iceText = new SpecialText(tileChar, Color.WHITE, new Color(147, 166, 199));
        } else {
            iceText = new SpecialText(tileChar, Color.WHITE, new Color(109, 133, 166));
        }
        target.getLevel().getOverlayTileLayer().editLayer(iceTile.getLocation(), iceText);
    }

    @Override
    public void onRemove(TagHolder owner) {
        if (owner instanceof Tile) {
            Tile tile = (Tile) owner;
            Tile overlay = tile.getLevel().getOverlayTileAt(tile.getLocation());
            if (tile.equals(overlay)){
                tile.getLevel().removeOverlayTile(overlay);
            }
        }
    }

    @Override
    public void onAdd(TagEvent e) {
        if (e.getTarget().hasTag(TagRegistry.ON_FIRE)) e.addFutureAction(event -> {
            e.getTarget().removeTag(getId());
            e.getTarget().addTag(TagRegistry.WET, e.getTarget());
            e.getTarget().addTag(TagRegistry.NO_REFREEZE, e.getTarget());
        });
    }

    @Override
    public Color getTagColor() {
        return new Color(184, 188, 230);
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
