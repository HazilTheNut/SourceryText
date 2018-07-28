package Game.Tags.EnchantmentTags;

import Data.Coordinate;
import Data.SerializationVersion;
import Game.Entities.Entity;
import Game.TagEvent;

import java.awt.*;

public class WarpEnchantmentTag extends EnchantmentTag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onDealDamage(TagEvent e) {
        e.addCancelableAction(event -> {
            if (e.getTarget() instanceof  Entity){
                Entity target = (Entity)e.getTarget();
                if (e.getSource() instanceof Entity) {
                    Entity source = (Entity) e.getSource();
                    Coordinate diff = target.getLocation().subtract(source.getLocation());
                    source.teleport(target.getLocation().add(diff.normalize()));
                }
            }
        });
    }

    @Override
    public Color getTagColor() {
        return new Color(151, 37, 255);
    }
}
