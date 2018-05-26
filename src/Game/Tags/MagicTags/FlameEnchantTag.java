package Game.Tags.MagicTags;

import Data.SerializationVersion;
import Game.TagEvent;
import Game.Tags.OnFireTag;

/**
 * Created by Jared on 4/15/2018.
 */
public class FlameEnchantTag extends OnFireTag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onAddThis(TagEvent e) {
        burnForever = true;
    }
}
