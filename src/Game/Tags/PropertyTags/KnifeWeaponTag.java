package Game.Tags.PropertyTags;

import Data.SerializationVersion;
import Game.TagEvent;

public class KnifeWeaponTag extends WeaponTypeTag {
    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onAddThis(TagEvent e) {
        //Does not mark this item as non-stacking.
    }
}
