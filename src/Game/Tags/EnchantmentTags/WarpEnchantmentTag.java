package Game.Tags.EnchantmentTags;

import Data.Coordinate;
import Data.SerializationVersion;
import Game.Entities.Entity;
import Game.Projectile;
import Game.TagEvent;
import Game.Tile;

import java.awt.*;

public class WarpEnchantmentTag extends EnchantmentTag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onDealDamage(TagEvent e) {
        e.addCancelableAction(event -> {
            Coordinate targetPos = null;
            if (e.getTarget() instanceof Entity)
                targetPos = ((Entity)e.getTarget()).getLocation();
            else if (e.getTarget() instanceof Tile)
                targetPos = ((Tile)e.getTarget()).getLocation();
            if (targetPos != null) {
                if (e.getTagOwner() instanceof Entity) {
                    Entity tagOwner = (Entity) e.getSource();
                    Coordinate diff = targetPos.subtract(tagOwner.getLocation());
                    tagOwner.teleport(targetPos.add(diff.normalize()));
                } else if (e.getTagOwner() instanceof Projectile && e.getSource() instanceof Entity) {
                    Projectile tagOwner = (Projectile) e.getTagOwner();
                    Coordinate diff = new Coordinate((int)Math.round(tagOwner.getNormalizedVelocityX()), (int)Math.round(tagOwner.getNormalizedVelocityY()));
                    ((Entity)e.getSource()).teleport(targetPos.add(diff.multiply(-1)));
                }
            }
        });
    }

    @Override
    public Color getTagColor() {
        return EnchantmentColors.WARP;
    }
}
