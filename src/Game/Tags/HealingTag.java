package Game.Tags;

import Game.TagEvent;
import Game.TagHolder;

/**
 * Created by Jared on 3/31/2018.
 */
public class HealingTag extends Tag{

    @Override
    public void onItemUse(TagEvent e, TagHolder user) {
        user.heal(5);
        e.setSuccess(true);
    }
}
