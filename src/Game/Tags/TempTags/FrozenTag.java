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
    private boolean isAttachedToIceOverlay = false;

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
                } else {
                    isAttachedToIceOverlay = true;
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
            if (isAttachedToIceOverlay){ //If the owning TagHolder is an overlay tile, chances are it is an ice tile.
                tile.getLevel().removeOverlayTile(overlay);
            }
        }
    }

    @Override
    public void onAdd(TagEvent e) {
        if (e.getTarget().hasTag(TagRegistry.ON_FIRE)) e.addFutureAction(event -> {
            e.getTarget().removeTag(getId());
            if (e.getTarget() instanceof Tile) {
                Tile target = (Tile) e.getTarget();
                meltOnto(target.getLevel().getBaseTileAt(target.getLocation()));
            } else {
                meltOnto(e.getTarget());
            }
        });
    }

    private void meltOnto(TagHolder holder){
        holder.addTag(TagRegistry.WET, holder);
        holder.addTag(TagRegistry.NO_REFREEZE, holder);
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
