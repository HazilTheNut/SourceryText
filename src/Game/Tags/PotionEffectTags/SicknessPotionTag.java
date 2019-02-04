package Game.Tags.PotionEffectTags;

import Data.SerializationVersion;
import Game.Registries.TagRegistry;
import Game.TagEvent;
import Game.Tags.Tag;

public class SicknessPotionTag extends Tag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onItemUse(TagEvent e) {
        e.getTarget().addTag(TagRegistry.POISON, e.getTagOwner());
        e.getTarget().addTag(TagRegistry.DIZZY, e.getTagOwner());
    }
}
