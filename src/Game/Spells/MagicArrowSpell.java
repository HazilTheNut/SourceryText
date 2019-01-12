package Game.Spells;

import Data.Coordinate;
import Data.SerializationVersion;
import Engine.SpecialText;
import Game.ArrowProjectile;
import Game.Entities.Entity;
import Game.GameInstance;
import Game.Item;
import Game.Projectile;
import Game.Registries.ItemRegistry;
import Game.Registries.TagRegistry;

import java.awt.*;

/**
 * Created by Jared on 4/19/2018.
 */
public class MagicArrowSpell extends Spell {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private final SpecialText icon = new SpecialText('*', new Color(87, 107, 173), new Color(50, 65, 120, 20));

    @Override
    public String getName() {
        return "Magic Arrow";
    }

    @Override
    public Color getColor() {
        return new Color(130, 255, 221);
    }

    @Override
    public int castSpell(Coordinate targetLoc, Entity spellCaster, GameInstance gi, int magicPower) {
        Item arrowItem = ItemRegistry.generateItem(ItemRegistry.ID_ARROW, gi);
        ArrowProjectile magicArrow = new ArrowProjectile(spellCaster, targetLoc, icon, arrowItem, 0);
        magicArrow.addTag(TagRegistry.DAMAGE_START + calculatePower(3, magicPower, 0.525), spellCaster);
        magicArrow.addTag(TagRegistry.FRAGILE, spellCaster);
        magicArrow.launchProjectile(calculatePower(15, magicPower, 0.5));
        return calculateCooldown(15, magicPower);
    }

    @Override
    public Spell copy() {
        return new MagicArrowSpell();
    }
}
