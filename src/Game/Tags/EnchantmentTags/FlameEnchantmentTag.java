package Game.Tags.EnchantmentTags;

import Data.SerializationVersion;
import Game.Registries.TagRegistry;
import Game.TagEvent;

import java.awt.*;

/**
 * Created by Jared on 4/15/2018.
 */
public class FlameEnchantmentTag extends EnchantmentTag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onContact(TagEvent e) {
        e.getTarget().addTag(TagRegistry.ON_FIRE, e.getSource());
    }

    @Override
    public void onFlyOver(TagEvent e) {
        e.getTarget().addTag(TagRegistry.ON_FIRE, e.getSource());
    }

    @Override
    public Color getTagColor() {
        return new Color(255, 131, 59);
    }
}
