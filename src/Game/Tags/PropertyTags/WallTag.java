package Game.Tags.PropertyTags;

import Game.Registries.TagRegistry;
import Game.TagEvent;
import Game.Tags.Tag;

/**
 * Created by Jared on 4/7/2018.
 */
public class WallTag extends Tag {

    @Override
    public void onAddThis(TagEvent e) {
        e.getTarget().addTag(TagRegistry.NO_PATHING, e.getSource());
    }
}
