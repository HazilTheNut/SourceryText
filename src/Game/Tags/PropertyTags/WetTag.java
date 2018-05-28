package Game.Tags.PropertyTags;

import Data.SerializationVersion;
import Engine.SpecialText;
import Game.Entities.Entity;
import Game.Registries.TagRegistry;
import Game.TagEvent;
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
            if (!e.getTarget().hasTag(TagRegistry.SHALLOW_WATER) && !e.getTarget().hasTag(TagRegistry.DEEP_WATER) && !tile.getLevel().getOverlayTiles().contains(tile)){
                createOverlayTile(tile);
                e.cancel();
            }
        }
    }

    private void createOverlayTile(Tile tile){
        Tile waterTile = new Tile(tile.getLocation(), "Water", tile.getLevel());
        tile.getLevel().addOverlayTile(waterTile);
        waterTile.addTag(TagRegistry.SHALLOW_WATER, tile);
        waterTile.addTag(TagRegistry.WET, tile);
        waterTile.addTag(TagRegistry.NO_REFREEZE, tile);
        tile.getLevel().getOverlayTileLayer().editLayer(tile.getLocation().getX(), tile.getLocation().getY(), new SpecialText(' ', Color.WHITE, new Color(50, 50, 255, 50)));
    }

    @Override
    public void onTurn(TagEvent e) {
        if (drying) lifetime--;
        if (lifetime == 0) e.addCancelableAction(event -> e.getSource().removeTag(TagRegistry.WET));
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
