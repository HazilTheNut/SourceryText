package Game.Tags.EnchantmentTags;

import Data.SerializationVersion;
import Game.Player;
import Game.TagEvent;

import java.awt.*;

public class ClarityEnchantmentTag extends EnchantmentTag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onDealDamage(TagEvent e) {
        e.addCancelableAction(event -> {
            if (e.getSource() instanceof Player) {
                Player source = (Player) e.getSource();
                source.decrementCooldowns();
            }
        });
    }

    @Override
    public Color getTagColor() {
        return EnchantmentColors.CLARITY;
    }
}
