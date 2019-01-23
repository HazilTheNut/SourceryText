package Game.Tags.PotionEffectTags;

import Data.SerializationVersion;
import Game.Registries.TagRegistry;
import Game.TagEvent;
import Game.Tags.Tag;

public class PotionEffectTag extends Tag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onTurn(TagEvent e) {
        if (e.getTagOwner().hasTag(TagRegistry.POTION))
            e.cancel();
    }
}
