package Game.Tags;

import Game.TagEvent;
import Game.TagHolder;

/**
 * Created by Jared on 4/1/2018.
 */
public class DamageTag extends Tag{

    @Override
    public void onItemUse(TagEvent e, TagHolder user) {
        user.receiveDamage(3);
        e.setSuccess(true);
    }
}
