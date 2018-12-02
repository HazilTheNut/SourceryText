package Game.Spells;

import Data.Coordinate;
import Data.SerializationVersion;
import Engine.SpecialText;
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
        return new Color(115, 220, 255);
    }

    @Override
    public int castSpell(Coordinate targetLoc, Entity spellCaster, GameInstance gi, int magicPower) {
        Projectile magicArrow = new Projectile(spellCaster, targetLoc, icon);
        magicArrow.addTag(TagRegistry.DAMAGE_START + calculatePower(3, magicPower, 0.525), spellCaster);
        Item arrowItem = ItemRegistry.generateItem(ItemRegistry.ID_ARROW, gi);
        magicArrow.getTags().addAll(arrowItem.getTags());
        magicArrow.launchProjectile(calculatePower(15, magicPower, 0.5));
        return calculateCooldown(15, magicPower);
    }

    @Override
    public Spell copy() {
        return new MagicArrowSpell();
    }
}
