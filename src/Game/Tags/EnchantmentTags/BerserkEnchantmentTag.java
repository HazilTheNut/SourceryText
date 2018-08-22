package Game.Tags.EnchantmentTags;

import Data.SerializationVersion;
import Game.Entities.BasicEnemy;
import Game.Entities.CombatEntity;
import Game.Registries.TagRegistry;
import Game.TagEvent;

import java.awt.*;

public class BerserkEnchantmentTag extends EnchantmentTag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onContact(TagEvent e) {
        if (e.getTarget() instanceof CombatEntity) {
            e.getTarget().addTag(TagRegistry.BERSERK, e.getSource());
            if (e.getTarget() instanceof BasicEnemy) {
                BasicEnemy target = (BasicEnemy) e.getTarget();
                CombatEntity newTarget = target.getNearestEnemy();
                target.setTarget(newTarget);
            }
        }
    }

    @Override
    public Color getTagColor() {
        return new Color(255, 65, 51);
    }
}
