package Game.Tags.EnchantmentTags;

import Data.SerializationVersion;
import Game.Registries.TagRegistry;
import Game.TagEvent;
import Game.Tags.LuminantTag;

import java.awt.*;

/**
 * Created by Jared on 4/15/2018.
 */
public class FlameEnchantmentTag extends EnchantmentTag implements LuminantTag {

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
        return EnchantmentColors.FLAME;
    }

    @Override
    public double getLuminance() {
        return 5;
    }
}
