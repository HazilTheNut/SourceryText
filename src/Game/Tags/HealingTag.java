package Game.Tags;

import Game.TagHolder;

/**
 * Created by Jared on 3/31/2018.
 */
public class HealingTag extends Tag{

    @Override
    public void onItemUse(TagHolder user) {
        user.heal(5);
    }
}
