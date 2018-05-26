package Game.Tags.PropertyTags;

import Data.SerializationVersion;
import Game.Item;
import Game.TagEvent;
import Game.Tags.Tag;

/**
 * Created by Jared on 5/18/2018.
 */
public class ImportantTag extends Tag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onAddThis(TagEvent e) {
        if (e.getSource() instanceof Item) {
            Item source = (Item) e.getSource();
            source.setStackable(Item.NO_QUANTITY);
        }
    }
}
