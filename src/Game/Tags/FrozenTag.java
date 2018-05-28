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
    private Tile iceTile;

    @Override
    public void onAddThis(TagEvent e) {
        if (e.getTarget().hasTag(TagRegistry.ON_FIRE)){
            e.getTarget().removeTag(TagRegistry.ON_FIRE);
            e.getTarget().addTag(TagRegistry.WET, e.getTarget());
            e.cancel();
        }
        e.addCancelableAction(event -> {
            if (e.getTarget() instanceof Tile && e.getTarget().hasTag(TagRegistry.WET)) {
                Tile target = (Tile) e.getTarget();
                createIceOverlayTile(target, e.getSource());
            }
        });
    }
    
    private void createIceOverlayTile(Tile target, TagHolder source){
        iceTile = new Tile(target.getLocation(), "Ice", target.getLevel());
        target.getLevel().addOverlayTile(iceTile);
        iceTile.addTag(TagRegistry.FROZEN, source);
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
        if (iceTile != null) {
            iceTile.getLevel().removeOverlayTile(iceTile);
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
