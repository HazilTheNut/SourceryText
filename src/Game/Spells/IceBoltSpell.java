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
public class IceBoltSpell extends Spell {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private final SpecialText icon = new SpecialText('*', new Color(154, 181, 190), new Color(92, 114, 120, 20));

    @Override
    public String getName() {
        return "Ice Bolt";
    }

    @Override
    public Color getColor() {
        return new Color(217, 242, 255);
    }

    @Override
    public int castSpell(Coordinate targetLoc, Entity spellCaster, GameInstance gi, int magicPower) {
        Projectile fireBolt = new Projectile(spellCaster, targetLoc, icon);
        fireBolt.addTag(TagRegistry.DAMAGE_START + calculatePower(15, magicPower, 0.4375), spellCaster);
        fireBolt.addTag(TagRegistry.FROST_ENCHANT,    spellCaster);
        fireBolt.launchProjectile(14);
        return calculateCooldown(25, magicPower);
    }

    @Override
    public Spell copy() {
        return new IceBoltSpell();
    }
}
