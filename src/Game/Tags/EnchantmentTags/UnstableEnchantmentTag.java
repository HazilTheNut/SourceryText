package Game.Tags.EnchantmentTags;

import Data.SerializationVersion;
import Game.Entities.Entity;
import Game.Registries.TagRegistry;
import Game.TagEvent;

import java.awt.*;

public class UnstableEnchantmentTag extends EnchantmentTag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onContact(TagEvent e) {
        if (e.getTarget() instanceof Entity) {
            e.getTarget().addTag(TagRegistry.UNSTABLE, e.getSource());
        }
    }

    @Override
    public Color getTagColor() {
        return new Color(255, 0, 68);
    }
}
