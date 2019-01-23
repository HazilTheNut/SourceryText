package Game.Tags;

import Data.SerializationVersion;
import Game.Item;
import Game.TagEvent;

public class PotionTag extends Tag{

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onContact(TagEvent e) {
        if (e.getTagOwner() instanceof Item) {
            Item tagOwner = (Item) e.getTagOwner();
            tagOwner.onItemUse(e.getTarget());
        }
    }
}
