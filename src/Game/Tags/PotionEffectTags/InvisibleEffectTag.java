package Game.Tags.PotionEffectTags;

import Data.SerializationVersion;
import Game.Entities.Entity;
import Game.TagEvent;
import Game.TagHolder;
import Game.Tags.Tag;
import Game.Tags.TempTags.TempTag;

import java.awt.*;

public class InvisibleEffectTag extends TempTag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    public InvisibleEffectTag(){
        LIFETIME_START = 25;
    }

    @Override
    public void onAddThis(TagEvent e) {
        super.onAddThis(e);
        if (e.getTagOwner() instanceof Entity) {
            Entity tagOwner = (Entity) e.getTagOwner();
            tagOwner.getSprite().setVisible(false);
        }
    }

    @Override
    public void onRemove(TagHolder owner) {
        if (owner instanceof Entity) {
            Entity entity = (Entity) owner;
            entity.getSprite().setVisible(true);
        }
    }

    @Override
    public Color getTagColor() {
        return new Color(118, 143, 143);
    }
}
