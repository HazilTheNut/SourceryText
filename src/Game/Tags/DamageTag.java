package Game.Tags;

import Game.TagEvent;
import Game.TagHolder;

/**
 * Created by Jared on 4/1/2018.
 */
public class DamageTag extends Tag{

    private int damageAmount;

    public DamageTag(int dmg){
        damageAmount = dmg;
    }

    @Override
    public void onItemUse(TagEvent e, TagHolder user) {
        user.receiveDamage(damageAmount);
        e.setSuccess(true);
    }
}
