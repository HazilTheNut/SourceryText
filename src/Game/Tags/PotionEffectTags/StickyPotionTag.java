package Game.Tags.PotionEffectTags;

import Data.SerializationVersion;
import Game.Item;
import Game.Registries.TagRegistry;
import Game.TagEvent;
import Game.Tags.Tag;

public class StickyPotionTag extends Tag {

    /**
     * StickyPotionTag:
     *
     * The "delivery mechanism" used by potions to give TagHolders the "Sticky" potion effect.
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onItemUse(TagEvent e) {
        e.getTarget().addTag(TagRegistry.EFFECT_STICKY, e.getTagOwner());
        e.setAmount(Item.EVENT_TURN_USED);
    }

}
