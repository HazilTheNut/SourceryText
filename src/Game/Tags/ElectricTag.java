package Game.Tags;

import Data.SerializationVersion;
import Engine.SpecialText;
import Game.Entities.CombatEntity;
import Game.Entities.Entity;
import Game.Projectile;
import Game.Registries.TagRegistry;
import Game.TagEvent;

import java.awt.*;
import java.util.ArrayList;

public class ElectricTag extends Tag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onContact(TagEvent e) {
        if (e.getTarget() instanceof Entity) {
            Entity target = (Entity) e.getTarget();
            target.onReceiveDamage(5, e.getSource(), target.getGameInstance());
            if (isConductive(target)) {
                ArrayList<Entity> entities = target.getGameInstance().getCurrentLevel().getEntities();
                for (Entity entity : entities) {
                    if (entity.getLocation().hypDistance(target.getLocation()) <= 8) shootProjectileAt(target, entity);
                }
            }
        }
    }

    private void shootProjectileAt(Entity source, Entity target){
        if (source != target) {
            Projectile zapProj = new Projectile(source, target.getLocation(), new SpecialText('+', new Color(255, 255, 50), new Color(255, 255, 50, 50)));
            zapProj.addTag(TagRegistry.DAMAGE_START + 5, source);
            zapProj.launchProjectile(source.getLocation().hypDistance(target.getLocation()) + 5, source.getGameInstance());
        }
    }

    private boolean isConductive(Entity e){
        return e.hasTag(TagRegistry.METALLIC) || (e instanceof CombatEntity && ((CombatEntity)e).getWeapon().hasTag(TagRegistry.METALLIC));
    }

    @Override
    public Color getTagColor() {
        return new Color(255, 255, 50);
    }
}
