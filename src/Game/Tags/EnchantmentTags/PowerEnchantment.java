package Game.Tags.EnchantmentTags;

import Data.SerializationVersion;
import Game.TagEvent;

import java.awt.*;

public class PowerEnchantment extends EnchantmentTag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onDealDamage(TagEvent e) {
        e.addCancelableAction(event -> e.getTagOwner().onReceiveDamage(1, e.getSource(), e.getGameInstance()));
        e.addFutureAction(event -> e.setAmount((int) (e.getAmount() * 1.5)));
    }

    @Override
    public Color getTagColor() {
        return EnchantmentColors.POWER;
    }
}
