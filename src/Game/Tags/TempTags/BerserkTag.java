package Game.Tags.TempTags;

import Data.SerializationVersion;
import Game.Tags.EnchantmentTags.EnchantmentColors;

import java.awt.*;

public class BerserkTag extends TempTag {
    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    public BerserkTag(){
        LIFETIME_START = 20;
    }

    @Override
    public Color getTagColor() {
        return EnchantmentColors.BERSERK;
    }
}
