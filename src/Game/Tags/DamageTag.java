package Game.Tags;

import Data.SerializationVersion;
import Game.TagEvent;

/**
 * Created by Jared on 4/1/2018.
 */
public class DamageTag extends Tag{

    /**
     * DamageTag:
     *
     * Amplifies damage dealt by an incremental amount.
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private int damageAmount;

    public DamageTag(int dmg, int id){
        damageAmount = dmg;
        setId(id);
        setName(String.format("Damage: %d", dmg));
    }

    @Override
    public void onDealDamage(TagEvent e) {
        e.setAmount(e.getAmount() + damageAmount);
    }

    public int getDamageAmount() {
        return damageAmount;
    }

    public void setDamageAmount(int damageAmount) {
        this.damageAmount = damageAmount;
    }

    @Override
    public Tag copy() {
        return new DamageTag(damageAmount, getId());
    }
}
