package Game.Tags;

import Data.SerializationVersion;
import Game.TagEvent;

/**
 * Created by Jared on 4/1/2018.
 */
public class DamageTag extends Tag{

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

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
