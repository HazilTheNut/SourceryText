package Game.Tags.MagicTags;

import Data.SerializationVersion;
import Game.Registries.TagRegistry;
import Game.TagEvent;
import Game.Tags.Tag;

import java.awt.*;

/**
 * Created by Jared on 4/25/2018.
 */
public class FrostEnchantmentTag extends Tag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onFlyOver(TagEvent e) {
        e.addCancelableAction(event -> e.getTarget().addTag(TagRegistry.FROZEN, e.getSource()));
    }

    public void onContact(TagEvent e){
        e.addCancelableAction(event -> e.getTarget().addTag(TagRegistry.FROZEN, e.getSource()));
    }

    @Override
    public Color getTagColor() {
        return new Color(102, 201, 255);
    }
}