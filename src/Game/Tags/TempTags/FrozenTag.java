package Game.Tags.TempTags;

import Data.SerializationVersion;
import Game.Entities.Entity;
import Game.OverlayTileGenerator;
import Game.Registries.TagRegistry;
import Game.TagEvent;
import Game.TagHolder;
import Game.Tags.Tag;
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
                    createIceOverlayTile(target);
                    e.addFutureAction(event -> target.removeTag(TagRegistry.FROZEN));
                }
            } else
                e.cancel();
        }
    }

    private void createIceOverlayTile(Tile target){
        OverlayTileGenerator tileGenerator = new OverlayTileGenerator();
        if (target.hasTag(TagRegistry.TILE_WALL))
            tileGenerator.createIceWallTile(target.getLocation(), target.getLevel());
        else
            tileGenerator.createIceTile(target.getLocation(), target.getLevel());
    }

    @Override
    public void onRemove(TagHolder owner) {
        if (owner instanceof Tile) {
            Tile tile = (Tile) owner;
            Tile overlay = tile.getLevel().getOverlayTileAt(tile.getLocation());
            if (tile.equals(overlay)){ //If the owning TagHolder is an overlay tile, chances are it is an ice tile.
                tile.getLevel().removeOverlayTile(overlay);
                if (tile.hasTag(TagRegistry.ON_FIRE))
                    tile.getLevel().getBaseTileAt(tile.getLocation()).addTag(TagRegistry.WET, owner);
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
