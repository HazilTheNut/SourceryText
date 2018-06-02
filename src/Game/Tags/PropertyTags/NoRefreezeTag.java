package Game.Tags.PropertyTags;

import Data.SerializationVersion;
import Game.TagEvent;
import Game.Tags.Tag;

public class NoRefreezeTag extends Tag {

    /**
     * NoRefreezeTag:
     *
     * When an ice spell passes over some fiery tiles, it dissipates the fire and replaces it with water.
     * To prevent the ice spell from immediately freezing the newly-created water, this tag is added to the wet tiles to stop that behavior.
     *
     * When the spell finishes, this tag self-destructs
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onTurn(TagEvent e) {
        e.addCancelableAction(event -> e.getSource().removeTag(getId())); //This works because the next turn is ran after the spell finishes its projectile motion.
    }
}
