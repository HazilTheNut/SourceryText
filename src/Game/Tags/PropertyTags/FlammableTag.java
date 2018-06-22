package Game.Tags.PropertyTags;

import Data.SerializationVersion;
import Game.Registries.TagRegistry;
import Game.TagEvent;
import Game.Tags.Tag;

/**
 * Created by Jared on 4/9/2018.
 */
public class FlammableTag extends Tag {
    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onFlyOver(TagEvent e) {
        if (e.getTarget().hasTag(TagRegistry.ON_FIRE)){
            e.addFutureAction(event -> e.getSource().addTag(TagRegistry.ON_FIRE, e.getTarget()));
        }
    }
}
