package Game.Tags.EnchantmentTags;

import Data.SerializationVersion;
import Game.Entities.Entity;
import Game.Registries.TagRegistry;
import Game.TagEvent;

import java.awt.*;

/**
 * Created by Jared on 4/15/2018.
 */
public class SlothEnchantmentTag extends EnchantmentTag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onContact(TagEvent e) {
        if (e.getTarget() instanceof Entity) {
            e.getTarget().addTag(TagRegistry.SLOTH, e.getSource());
        }
    }

    @Override
    public Color getTagColor() {
        return EnchantmentColors.SLOTH;
    }
}
