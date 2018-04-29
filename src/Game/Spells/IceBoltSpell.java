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
public class IceBoltSpell extends Spell {

    private final SpecialText icon = new SpecialText('*', new Color(154, 181, 190), new Color(92, 114, 120, 20));

    @Override
    public String getName() {
        return "Ice Bolt";
    }

    @Override
    public int castSpell(Coordinate targetLoc, Entity spellCaster, GameInstance gi) {
        Projectile fireBolt = new Projectile(spellCaster, targetLoc, icon, gi.getLayerManager());
        fireBolt.addTag(TagRegistry.DAMAGE_START + 6, spellCaster);
        fireBolt.addTag(TagRegistry.FROST_ENCHANT,    spellCaster);
        fireBolt.launchProjectile(14, gi);
        return 25;
    }
}
