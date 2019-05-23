package Game.Tags.PotionEffectTags;

import Data.SerializationVersion;
import Game.Item;
import Game.Registries.TagRegistry;
import Game.TagEvent;
import Game.Tags.Tag;

public class InvisiblePotionTag extends Tag {

    /**
     * MagneticPotionTag:
     *
     * The "delivery mechanism" used by potions to give TagHolders the "Invisible" potion effect.
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onItemUse(TagEvent e) {
        e.getTarget().addTag(TagRegistry.EFFECT_INVISIBLE, e.getTagOwner());
        e.setAmount(Item.EVENT_TURN_USED);
    }
}