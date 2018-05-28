package Game.Tags.PropertyTags;

import Data.SerializationVersion;
import Engine.SpecialText;
import Game.Entities.Entity;
import Game.Registries.TagRegistry;
import Game.TagEvent;
import Game.TagHolder;
import Game.Tags.Tag;
import Game.Tile;

import java.awt.*;

/**
 * Created by Jared on 4/29/2018.
 */
public class WetTag extends Tag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private int LIFETIME_START = 10;
    private int lifetime = LIFETIME_START;
    private boolean drying;

    @Override
    public void onAddThis(TagEvent e) {
        if (e.getTarget() instanceof Entity){
            drying = true;
            lifetime = LIFETIME_START;
        } else if (e.getTarget() instanceof Tile){
            Tile tile = (Tile)e.getTarget();
            Tile baseTile = tile.getLevel().getBaseTileAt(tile.getLocation());
            if (baseTile != null) {
                if (!baseTile.hasTag(TagRegistry.SHALLOW_WATER) && !baseTile.hasTag(TagRegistry.DEEP_WATER)) {
                    tile.getLevel().getOverlayTileLayer().editLayer(tile.getLocation(), new SpecialText(' ', Color.WHITE, new Color(15, 15, 30, 30)));
                }
            }
        }
    }

    @Override
    public void onTurn(TagEvent e) {
        if (drying) lifetime--;
        if (lifetime == 0) e.addFutureAction(event -> e.getSource().removeTag(TagRegistry.WET));
    }

    @Override
    public void onRemove(TagHolder owner) {
        if (owner instanceof Tile) {
            Tile tile = (Tile) owner;
            if (tile.getLevel().getOverlayTileAt(tile.getLocation()) == null){
                tile.getLevel().getOverlayTileLayer().editLayer(tile.getLocation(), null);
            }
        }
    }

    @Override
    public void onContact(TagEvent e) {
        super.onContact(e);
        if (e.getSource() instanceof Tile) {
            if (!e.getTarget().hasTag(TagRegistry.WET))
                e.getTarget().addTag(TagRegistry.WET, e.getSource());
            else
                ((WetTag)e.getTarget().getTag(getId())).lifetime = LIFETIME_START;
        }
    }

    @Override
    public Color getTagColor() {
        return new Color(50, 50, 166, 50);
    }
}
