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
public class MagicBoltSpell extends Spell {

    private final SpecialText icon = new SpecialText('*', new Color(87, 107, 173), new Color(50, 65, 120, 20));

    @Override
    public String getName() {
        return "Magic Bolt";
    }

    @Override
    public int castSpell(Coordinate targetLoc, Entity spellCaster, GameInstance gi) {
        Projectile magicBolt = new Projectile(spellCaster, targetLoc, icon, gi.getLayerManager());
        magicBolt.addTag(TagRegistry.getTag(TagRegistry.DAMAGE_START + 4), spellCaster);
        magicBolt.launchProjectile(25, gi);
        return 15;
    }
}
