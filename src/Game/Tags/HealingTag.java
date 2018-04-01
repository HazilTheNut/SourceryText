package Game.Tags;

import Game.TagEvent;
import Game.TagHolder;

/**
 * Created by Jared on 3/31/2018.
 */
public class HealingTag extends Tag{

    private int healingAmount;

    public HealingTag(int amount){
        healingAmount = amount;
    }

    @Override
    public void onItemUse(TagEvent e, TagHolder user) {
        user.heal(healingAmount);
        e.setSuccess(true);
    }
}
