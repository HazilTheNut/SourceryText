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

    /**
     * FrozenTag:
     *
     * The Tag that defines a frozen TagHolder
     *
     * For Entities:
     *  > Prevents all actions until a timer reaches zero (starts at 4)
     *
     * For Tiles:
     *  > If the tile is wet, cancels adding this tag. It then creates an overlay tile.
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private int duration = 4;

    @Override
    public void onAddThis(TagEvent e) {
        if (e.getTarget() instanceof Tile) {
            if (e.getTarget().hasTag(TagRegistry.WET)) { //If it is a wet Tile
                Tile target = (Tile) e.getTarget();
                Tile overlay = target.getLevel().getOverlayTileAt(target.getLocation());
                if (overlay == null) { //This will return false for the new overlay tile, because it will already be added by the time this method is ran again
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
        target.getLevel().addOverlayTile(iceTile); //When onAddThis() is reran, there WILL be an overlay tile this time.
        iceTile.addTag(TagRegistry.WET, source);
        iceTile.addTag(TagRegistry.FROZEN, source); //...and here is where onAddThis() is ran.
        iceTile.removeTag(TagRegistry.WET);
        iceTile.addTag(TagRegistry.FLAMMABLE, source); //The ice can't melt if it cannot be set on fire.
        SpecialText iceText;
        int hash = 23 * 113 * iceTile.getLocation().getX() + iceTile.getLocation().getY(); //Could use improvement
        DebugWindow.reportf(DebugWindow.MISC, "FrozenTag","Ice Hash: %1$d", hash);
        char tileChar = (hash % 7 == 0) ? '/' : ' ';
        if (target.hasTag(TagRegistry.TILE_WALL)) {
            iceTile.addTag(TagRegistry.TILE_WALL, source);
            iceTile.addTag(TagRegistry.NO_PATHING, source);
            iceText = new SpecialText(tileChar, Color.WHITE, new Color(147, 166, 199));
        } else {
            iceText = new SpecialText(tileChar, Color.WHITE, new Color(109, 133, 166));
        }
        target.getLevel().getOverlayTileLayer().editLayer(iceTile.getLocation(), iceText); //Draws the ice tile
    }

    @Override
    public void onRemove(TagHolder owner) {
        if (owner instanceof Tile) {
            Tile tile = (Tile) owner;
            Tile overlay = tile.getLevel().getOverlayTileAt(tile.getLocation());
            if (tile.equals(overlay)){ //If the owning TagHolder is an overlay tile, chances are it is an ice tile.
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
    public String getName() {
        return String.format("%1$s (%2$d)", super.getName(), duration);
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
