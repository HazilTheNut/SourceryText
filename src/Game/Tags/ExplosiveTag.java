package Game.Tags;

import Data.SerializationVersion;
import Game.Entities.Entity;
import Game.Explosion;
import Game.Registries.TagRegistry;
import Game.TagEvent;

import java.awt.*;

/**
 * Created by Riley on 1/16/2019.
 */

public class ExplosiveTag extends Tag {
    /**
     * ExplosiveTag:
     *
     * The Tag means that something can go Boom!
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;
    private static final int damage = 5;
    private boolean boomed = false;

    @Override
    public Color getTagColor() {
        return new Color(134, 82, 11);
    }

    @Override
    public void onContact(TagEvent e) {
        if (e.getTarget().hasTag(TagRegistry.ON_FIRE))
            boom((Entity) e.getTagOwner());
    }

    @Override
    public void onEntityDestruct(Entity owner) {
        boom(owner);
    }

    @Override
    public void onReceiveDamage(TagEvent e) {
        boom((Entity) e.getTagOwner());
    }

    private void boom(Entity owner) {
        if (!boomed) {
            boomed = true; // because recursion
            Explosion explosion = new Explosion();
            explosion.addTag(TagRegistry.ON_FIRE, owner);
            explosion.explode(damage, owner.getLocation(), owner.getGameInstance(), getTagColor());
            owner.removeTag(TagRegistry.EXPLOSIVE);
        }
    }
}
