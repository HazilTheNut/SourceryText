package Game.Tags.EnchantmentTags;

import Data.SerializationVersion;
import Game.Registries.TagRegistry;
import Game.TagEvent;
import Game.Tags.TempTags.PoisonTag;

import java.awt.*;

public class VenomEnchantmentTag extends EnchantmentTag {

    /**
     * VenomEnchantmentTag:
     *
     * The Tag that unconditionally transmits poison upon contact.
     *
     * For All TagHolders:
     * > Transmits a PoisonTag upon contact, if the target is living.
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onContact(TagEvent e) {
        PoisonTag poisonTag = new PoisonTag();
        if (e.getTarget().hasTag(TagRegistry.LIVING))
            poisonTag.transmit(e.getTarget());
    }

    @Override
    public Color getTagColor() {
        return new Color(121, 226, 0);
    }
}
