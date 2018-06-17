package Game.Spells;

import Data.Coordinate;
import Data.SerializationVersion;
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

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private final SpecialText icon = new SpecialText('*', new Color(87, 107, 173), new Color(50, 65, 120, 20));

    @Override
    public String getName() {
        return "Magic Bolt";
    }

    @Override
    public int castSpell(Coordinate targetLoc, Entity spellCaster, GameInstance gi, int magicPower) {
        Projectile magicBolt = new Projectile(spellCaster, targetLoc, icon, gi.getLayerManager());
        magicBolt.addTag(TagRegistry.DAMAGE_START + calculateDamage(4, magicPower), spellCaster);
        magicBolt.launchProjectile(25, gi);
        return calculateCooldown(15, magicPower);
    }
}
