package Game.Spells;

import Data.Coordinate;
import Data.SerializationVersion;
import Engine.Layer;
import Engine.SpecialText;
import Game.Entities.Entity;
import Game.GameInstance;
import Game.Projectile;
import Game.Registries.TagRegistry;
import Game.UI.InventoryPanel;

import java.awt.*;

/**
 * Created by Jared on 4/19/2018.
 */
public class ThunderBoltSpell extends Spell {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private static final int BASE_DAMAGE   = 15;
    private static final int BASE_RANGE    = 10;
    private static final int BASE_COOLDOWN = 32;

    private static final double SCALAR_DAMAGE = 0.5625;
    private static final double SCALAR_RANGE  = 0.25;

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
        thunderBolt.addTag(TagRegistry.DAMAGE_START + calculatePower(BASE_DAMAGE, magicPower, SCALAR_DAMAGE), spellCaster);
        thunderBolt.addTag(TagRegistry.ELECTRIC_ENCHANT, spellCaster);
        thunderBolt.launchProjectile(calculatePower(BASE_RANGE, magicPower, SCALAR_RANGE));
        return calculateCooldown(BASE_COOLDOWN, magicPower);
    }

    @Override
    public int getDescriptionHeight() {
        return 5;
    }

    @Override
    public Layer drawDescription(Layer baseLayer, int magicPower) {
        //Draw flavor text
        baseLayer.inscribeString("Fires an energetic\nbolt of lightning", 1, 1, InventoryPanel.FONT_WHITE, true);
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
        return new ThunderBoltSpell();
    }
}
