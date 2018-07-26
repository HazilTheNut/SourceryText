package Game.Tags.EnchantmentTags;

import Data.SerializationVersion;
import Game.TagEvent;

import java.awt.*;

public class ThornEnchantmentTag extends EnchantmentTag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onDealDamage(TagEvent e) {
        e.addCancelableAction(event -> e.getSource().onReceiveDamage(3, e.getSource(), e.getGameInstance()));
        e.setAmount(e.getAmount() + 5);
    }

    @Override
    public Color getTagColor() {
        return new Color(87, 189, 74);
    }
}
