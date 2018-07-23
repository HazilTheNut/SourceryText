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
public class ThunderBoltSpell extends Spell {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private final SpecialText icon = new SpecialText('*', new Color(171, 171, 87), new Color(120, 120, 50, 20));

    @Override
    public String getName() {
        return "Thunder Bolt";
    }

    @Override
    public Color getColor() {
        return new Color(255, 255, 153);
    }

    @Override
    public int castSpell(Coordinate targetLoc, Entity spellCaster, GameInstance gi, int magicPower) {
        Projectile thunderBolt = new Projectile(spellCaster, targetLoc, icon);
        thunderBolt.addTag(TagRegistry.DAMAGE_START + calculateDamage(15, magicPower), spellCaster);
        thunderBolt.addTag(TagRegistry.ELECTRIC, spellCaster);
        thunderBolt.launchProjectile(20);
        return calculateCooldown(32, magicPower);
    }

    @Override
    public Spell copy() {
        return new ThunderBoltSpell();
    }
}
