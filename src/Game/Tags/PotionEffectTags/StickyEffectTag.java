package Game.Tags.PotionEffectTags;

import Data.SerializationVersion;
import Game.Entities.Entity;
import Game.TagEvent;
import Game.TagHolder;
import Game.Tags.Tag;
import Game.Tags.TempTags.TempTag;
import Game.Tile;

import java.awt.*;

public class StickyEffectTag extends TempTag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    public StickyEffectTag(){
        LIFETIME_START = 15;
    }

    @Override
    public void onAddThis(TagEvent e) {
        super.onAddThis(e);
        if (e.getTagOwner() instanceof Tile) {
            Tile tagOwner = (Tile) e.getTagOwner();
            tagOwner.updateTileTagColor();
        }
    }

    @Override
    public void onRemove(TagHolder owner) {
        if (owner instanceof Tile) {
            Tile tile = (Tile) owner;
            tile.updateTileTagColor();
        }
    }

    @Override
    public void onEntityMove(TagEvent e) {
        e.cancel();
    }

    @Override
    public void onContact(TagEvent e) {
        if (e.getSource() instanceof Tile && e.getTarget() instanceof Entity) {
            e.addFutureAction(event -> event.getTarget().addTag(this, event.getSource()));
        }
    }

    @Override
    public Color getTagColor() {
        return new Color(150, 225, 100);
    }

    @Override
    public Color getTagTileColor() {
        return new Color(150, 225, 100, 50);
    }
}
