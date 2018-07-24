package Game.Tags.EnchantmentTags;

import Data.SerializationVersion;
import Game.Entities.CombatEntity;
import Game.TagEvent;

import java.awt.*;

public class DuelingEnchantmentTag extends EnchantmentTag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private CombatEntity target;
    private int targetTimer = 0;

    @Override
    public void onDealDamage(TagEvent e) {
        CombatEntity ce = null;
        if (e.getTarget() instanceof CombatEntity) {
            ce = (CombatEntity) e.getTarget();
        }
        if (ce != null){
            if (ce.equals(target)){
                e.setAmount(e.getAmount() + 10);
            } else {
                target = ce;
                targetTimer = 2;
            }
        }
    }

    @Override
    public void onTurn(TagEvent e) {
        if (target != null){
            targetTimer--;
            if (targetTimer == 0)
                target = null;
        }
    }

    @Override
    public Color getTagColor() {
        return new Color (255, 56, 79);
    }
}
