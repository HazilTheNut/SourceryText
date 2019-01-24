package Game.Tags.EnchantmentTags;

import Data.SerializationVersion;
import Game.Tags.Tag;

public class EnchantmentTag extends Tag {
    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public int getTagType() {
        return Tag.TYPE_ENCHANTMENT;
    }
}
