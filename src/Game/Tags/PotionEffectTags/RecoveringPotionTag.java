package Game.Tags.PotionEffectTags;

import Data.SerializationVersion;
import Game.Item;
import Game.Registries.TagRegistry;
import Game.TagEvent;
import Game.Tags.Tag;

public class RecoveringPotionTag extends Tag {

    /**
     * RecoveringPotionTag:
     *
     * The "delivery mechanism" used by potions to give TagHolders the "Mending" potion effect.
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onItemUse(TagEvent e) {
        e.getTarget().addTag(TagRegistry.EFFECT_RECOVERING, e.getTagOwner());
        e.setAmount(Item.EVENT_TURN_USED);
    }
}
