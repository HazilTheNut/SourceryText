package Game.Tags.EnchantmentTags;

import Data.SerializationVersion;
import Game.TagEvent;

import java.awt.*;

public class RegenEnchantmentTag extends EnchantmentTag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private int regenTimer;
    private int originalHealth = -1;

    @Override
    public void onTurn(TagEvent e) {
        if (originalHealth == -1) originalHealth = e.getSource().getCurrentHealth();
        if (regenTimer < 1 && e.getSource().getCurrentHealth() < originalHealth)
            e.getSource().heal(1);
        if (regenTimer > 0) regenTimer--;
    }

    @Override
    public void onReceiveDamage(TagEvent e) {
        regenTimer = 25;
    }

    @Override
    public Color getTagColor() {
        return EnchantmentColors.REGEN;
    }
}
