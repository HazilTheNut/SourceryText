package Game.Spells;

import Data.Coordinate;
import Data.SerializationVersion;
import Engine.Layer;
import Engine.SpecialText;
import Game.ArrowProjectile;
import Game.Entities.Entity;
import Game.GameInstance;
import Game.Item;
import Game.Projectile;
import Game.Registries.ItemRegistry;
import Game.Registries.TagRegistry;
import Game.UI.InventoryPanel;

import java.awt.*;

/**
 * Created by Jared on 4/19/2018.
 */
public class MagicArrowSpell extends Spell {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private final SpecialText icon = new SpecialText('*', new Color(87, 107, 173), new Color(50, 65, 120, 20));

    private static final int BASE_DAMAGE   = 3;
    private static final int BASE_RANGE    = 8;
    private static final int BASE_COOLDOWN = 18;

    private static final double SCALAR_DAMAGE = 0.525;
    private static final double SCALAR_RANGE  = 0.3;

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
        ArrowProjectile magicArrow = new ArrowProjectile(spellCaster, targetLoc, icon, 0, arrowItem, arrowItem);
        magicArrow.addTag(TagRegistry.DAMAGE_START + calculatePower(BASE_DAMAGE, magicPower, SCALAR_DAMAGE), spellCaster);
        magicArrow.addTag(TagRegistry.FRAGILE, spellCaster);
        magicArrow.launchProjectile(calculatePower(BASE_RANGE, magicPower, SCALAR_RANGE));
        return calculateCooldown(BASE_COOLDOWN, magicPower);
    }

    @Override
    public int getDescriptionHeight() {
        return 5;
    }

    @Override
    public Layer drawDescription(Layer baseLayer, int magicPower) {
        //Draw flavor text
        baseLayer.inscribeString("Fires an arrow\nmade of magic", 1, 1, InventoryPanel.FONT_WHITE, true);
        baseLayer.inscribeString(" ~~~ ", 1, 3, InventoryPanel.FONT_GRAY);
        //Draw base stats
        baseLayer.inscribeString(String.format("Damage   : %1$d", BASE_DAMAGE),   1, 4, InventoryPanel.FONT_WHITE);
        baseLayer.inscribeString(String.format("Range    : %1$d", BASE_RANGE),    1, 5, InventoryPanel.FONT_WHITE);
        baseLayer.inscribeString(String.format("Cooldown : %1$d", BASE_COOLDOWN), 1, 6, InventoryPanel.FONT_WHITE);
        //Draw modifiers
        baseLayer.inscribeString(String.format("(+%1$d)", calculatePower(0, magicPower, SCALAR_DAMAGE)), 15, 4, InventoryPanel.FONT_BLUE);
        baseLayer.inscribeString(String.format("(+%1$d)", calculatePower(0, magicPower, SCALAR_RANGE)),  15, 5, InventoryPanel.FONT_BLUE);
        baseLayer.inscribeString(String.format("(-%1$d)", BASE_COOLDOWN - calculateCooldown(BASE_COOLDOWN, magicPower)),  15, 6, InventoryPanel.FONT_BLUE);
        return baseLayer;
    }

    @Override
    public Spell copy() {
        return new MagicArrowSpell();
    }
}
