package Game.Tags.EnchantmentTags;

import Data.Coordinate;
import Data.SerializationVersion;
import Game.Entities.Entity;
import Game.GameInstance;
import Game.Projectile;
import Game.Registries.TagRegistry;
import Game.TagEvent;

import java.awt.*;

public class ForceEnchantmentTag extends EnchantmentTag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onDealDamage(TagEvent e) {
        e.addCancelableAction(event -> {
            if (e.getTarget() instanceof  Entity){
                Entity target = (Entity)e.getTarget();
                if (e.getSource() instanceof Entity) {
                    Entity source = (Entity) e.getSource();
                    Coordinate diff = target.getLocation().subtract(source.getLocation());
                    launchEntity(target, diff.getX(), diff.getY(), e.getGameInstance());
                } else if (e.getSource() instanceof Projectile) {
                    Projectile source = (Projectile) e.getSource();
                    launchEntity(target, source.getXvelocity(), source.getYvelocity(), e.getGameInstance());
                }
            }
        });
    }

    private void launchEntity(Entity e, double dx, double dy, GameInstance gi){
        Coordinate startPos = e.getLocation().copy();
        for (int i = 1; i <= 4; i++) {
            Coordinate newLoc = new Coordinate((int)Math.round(startPos.getX() + (dx * i)), (int)Math.round(startPos.getY() + (dy * i)));
            if (gi.isSpaceAvailable(newLoc, TagRegistry.NO_PATHING) || newLoc.equals(e.getLocation())){
                e.teleport(newLoc);
            } else
                return;
            if (gi.getTileAt(newLoc).hasTag(TagRegistry.SLIDING))
                return;
            //Wait a little to simulate the effect of motion
            try {
                Thread.sleep(100);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public Color getTagColor() {
        return new Color(248, 255, 115);
    }
}
