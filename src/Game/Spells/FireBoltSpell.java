package Game.Spells;

import Data.Coordinate;
import Engine.SpecialText;
import Game.Entities.Entity;
import Game.GameInstance;
import Game.Projectile;
import Game.Registries.TagRegistry;

import java.awt.*;

/**
 * Created by Jared on 4/19/2018.
 */
public class FireBoltSpell extends Spell {

    private final SpecialText icon = new SpecialText('*', new Color(173, 105, 87), new Color(120, 65, 50, 20));

    @Override
    public String getName() {
        return "Fire Bolt";
    }

    @Override
    public int castSpell(Coordinate targetLoc, Entity spellCaster, GameInstance gi) {
        Projectile fireBolt = new Projectile(spellCaster, targetLoc, icon, gi.getLayerManager());
        fireBolt.addTag(TagRegistry.getTag(TagRegistry.DAMAGE_START + 6), spellCaster);
        fireBolt.addTag(TagRegistry.getTag(TagRegistry.FLAMMABLE), spellCaster);
        fireBolt.addTag(TagRegistry.getTag(TagRegistry.ON_FIRE), spellCaster);
        fireBolt.launchProjectile(12, gi);
        return 22;
    }
}
