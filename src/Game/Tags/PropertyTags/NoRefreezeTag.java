package Game.Tags.PropertyTags;

import Data.SerializationVersion;
import Game.TagEvent;
import Game.Tags.Tag;

public class NoRefreezeTag extends Tag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onTurn(TagEvent e) {
        e.addCancelableAction(event -> e.getSource().removeTag(getId()));
    }
}
