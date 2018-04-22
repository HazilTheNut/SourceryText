package Game.Tags.PropertyTags;

import Game.TagEvent;
import Game.Tags.OnFireTag;

/**
 * Created by Jared on 4/15/2018.
 */
public class FlameEnchantTag extends OnFireTag {
    @Override
    public void onAddThis(TagEvent e) {
        burnForever = true;
    }
}
