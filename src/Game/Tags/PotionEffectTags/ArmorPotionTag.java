package Game.Tags.PotionEffectTags;

import Data.SerializationVersion;
import Game.Registries.TagRegistry;
import Game.TagEvent;
import Game.Tags.Tag;

public class ArmorPotionTag extends Tag {

    /**
     * MagneticPotionTag:
     *
     * The "delivery mechanism" used by potions to give TagHolders the "Armor" potion effect.
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onItemUse(TagEvent e) {
        e.getTarget().addTag(TagRegistry.EFFECT_ARMOR, e.getTagOwner());
    }
}