package Game.Tags;

import Game.Registries.TagRegistry;
import Game.TagEvent;

/**
 * Created by Jared on 4/25/2018.
 */
public class FrostEnchantmentTag extends Tag{
    @Override
    public void onFlyOver(TagEvent e) {
        e.addCancelableAction(event -> e.getTarget().addTag(TagRegistry.FROZEN, e.getSource()));
    }
}