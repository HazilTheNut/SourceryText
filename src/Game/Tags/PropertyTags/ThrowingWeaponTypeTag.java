package Game.Tags.PropertyTags;

import Data.SerializationVersion;
import Game.Item;
import Game.TagEvent;

public class ThrowingWeaponTypeTag extends WeaponTypeTag {
    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onWeaponSwing(TagEvent e) {
        if (e.getTarget() instanceof Item) {
            Item target = (Item) e.getTarget();
            target.decrementQty();
        }
    }
}
