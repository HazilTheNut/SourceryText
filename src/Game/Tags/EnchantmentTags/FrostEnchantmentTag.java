package Game.Tags.EnchantmentTags;

import Data.SerializationVersion;
import Game.Registries.TagRegistry;
import Game.TagEvent;

import java.awt.*;

/**
 * Created by Jared on 4/25/2018.
 */
public class FrostEnchantmentTag extends EnchantmentTag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onFlyOver(TagEvent e) {
        e.addCancelableAction(event -> transmit(e));
    }

    public void onContact(TagEvent e){
        e.addCancelableAction(event -> transmit(e));
    }

    private void transmit(TagEvent e){
        if (!e.getTarget().hasTag(TagRegistry.NO_REFREEZE))
            e.getTarget().addTag(TagRegistry.FROZEN, e.getSource());
    }

    @Override
    public Color getTagColor() {
        return new Color(102, 201, 255);
    }
}