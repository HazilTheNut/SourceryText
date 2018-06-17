package Game.Tags.PropertyTags;

import Data.SerializationVersion;
import Game.TagEvent;
import Game.Tags.Tag;

public class ImmovableTag extends Tag {
    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onMove(TagEvent e) {
        e.cancel();
    }
}
