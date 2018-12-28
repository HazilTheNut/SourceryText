package Game.Tags.EnchantmentTags;

import Data.SerializationVersion;
import Game.TagEvent;

import java.awt.*;

public class VampireEnchantmentTag extends EnchantmentTag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onDealDamage(TagEvent e) {
        e.addCancelableAction(event -> e.getSource().heal(e.getAmount() / 2));
    }

    @Override
    public Color getTagColor() {
        return EnchantmentColors.VAMPIRE;
    }
}
