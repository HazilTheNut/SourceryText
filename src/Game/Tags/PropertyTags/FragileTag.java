package Game.Tags.PropertyTags;

import Data.SerializationVersion;
import Game.TagEvent;
import Game.Tags.Tag;

public class FragileTag extends Tag {
    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onContact(TagEvent e) {
        if (e.getAmount() == Tag.CONTACT_HEAVY){
            e.getTagOwner().selfDestruct();
        }
    }
}
