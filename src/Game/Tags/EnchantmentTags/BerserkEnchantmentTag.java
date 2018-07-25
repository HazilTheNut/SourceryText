package Game.Tags.EnchantmentTags;

import Data.Coordinate;
import Data.SerializationVersion;
import Game.Entities.BasicEnemy;
import Game.Entities.CombatEntity;
import Game.Entities.Entity;
import Game.GameInstance;
import Game.Registries.TagRegistry;
import Game.TagEvent;

import java.awt.*;

public class BerserkEnchantmentTag extends EnchantmentTag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onContact(TagEvent e) {
        if (e.getTarget() instanceof CombatEntity) {
            if (e.getTarget() instanceof BasicEnemy) {
                BasicEnemy target = (BasicEnemy) e.getTarget();
                BasicEnemy nearest = getNearestBasicEnemy(target.getLocation(), e.getGameInstance());
                if (nearest != null) {
                    target.setTarget(nearest);
                } else
                    return;
            }
            e.getTarget().addTag(TagRegistry.BERSERK, e.getSource());
        }
    }

    private BasicEnemy getNearestBasicEnemy(Coordinate pos, GameInstance gi){
        double lowestDistance = Double.MAX_VALUE;
        BasicEnemy target = null;
        for (Entity e : gi.getCurrentLevel().getEntities()){
            if (e instanceof BasicEnemy) {
                BasicEnemy basicEnemy = (BasicEnemy) e;
                double dist = basicEnemy.getLocation().hypDistance(pos);
                if (dist < lowestDistance && dist > 0.05){
                    target = basicEnemy;
                    lowestDistance = dist;
                }
            }
        }
        return target;
    }

    @Override
    public Color getTagColor() {
        return new Color(255, 65, 51);
    }
}
