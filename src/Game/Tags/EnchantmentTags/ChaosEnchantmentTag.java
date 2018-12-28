package Game.Tags.EnchantmentTags;

import Data.Coordinate;
import Data.SerializationVersion;
import Game.Entities.Entity;
import Game.GameInstance;
import Game.Projectile;
import Game.Registries.TagRegistry;
import Game.Spells.LocumancySpell;
import Game.TagEvent;

import java.awt.*;
import java.util.Random;

public class ChaosEnchantmentTag extends EnchantmentTag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onDealDamage(TagEvent e) {
        e.addCancelableAction(event -> {
            if (e.getTarget() instanceof  Entity){
                Entity target = (Entity)e.getTarget();
                Random random = new Random();
                Coordinate relativePos = new Coordinate(random.nextInt(9) - 4, random.nextInt(9) - 4);
                LocumancySpell locumancySpell = new LocumancySpell();
                if (e.getGameInstance().isSpaceAvailable(target.getLocation().add(relativePos), TagRegistry.NO_PATHING))
                    locumancySpell.teleportEntity(target, target.getLocation().add(relativePos));
            }
        });
    }

    @Override
    public Color getTagColor() {
        return EnchantmentColors.CHAOS;
    }
}
