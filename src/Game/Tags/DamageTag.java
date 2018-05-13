package Game.Tags;

import Game.TagEvent;

/**
 * Created by Jared on 4/1/2018.
 */
public class DamageTag extends Tag{

    public int damageAmount;

    public DamageTag(int dmg){
        damageAmount = dmg;
    }

    /*
    @Override
    public void onItemUse(TagEvent e) {
        e.addCancelableAction(event -> e.getTarget().receiveDamage(damageAmount));
        e.setSuccess(true);
    }
    */

    @Override
    public void onDealDamage(TagEvent e) {
        e.setAmount(e.getAmount() + damageAmount);
    }

    public int getDamageAmount() {
        return damageAmount;
    }
}
