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
        }
    }

    @Override
    public Color getTagColor() {
        return EnchantmentColors.BERSERK;
    }
}
