package Game.Tags.PropertyTags;

import Data.SerializationVersion;
import Game.Item;
import Game.TagEvent;
import Game.Tags.Tag;

/**
 * Created by Jared on 5/18/2018.
 */
public class SpecialItemTag extends Tag {
    /**
     * SpecialItemTag:
     *
     * Declares in item to be "special," which currently just makes an item non-stacking and list no quantity.
     * Items that should not degrade with usage are perfect candidates for making them "special"
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onAddThis(TagEvent e) {
        if (e.getSource() instanceof Item) {
            Item source = (Item) e.getSource();
            source.setStackable(Item.NO_QUANTITY);
        }
    }
}
