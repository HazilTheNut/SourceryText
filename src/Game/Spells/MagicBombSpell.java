package Game.Spells;

import Data.Coordinate;
import Data.EntityStruct;
import Data.SerializationVersion;
import Engine.Layer;
import Game.Entities.Entity;
import Game.Entities.MagicBomb;
import Game.GameInstance;
import Game.Registries.EntityRegistry;
import Game.Registries.TagRegistry;
import Game.UI.InventoryPanel;

import java.awt.*;

public class MagicBombSpell extends Spell {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public String getName() {
        return "Magic Bomb";
    }

    private static final int BASE_DAMAGE   = 15;
    private static final int BASE_COOLDOWN = 30;

    private static final double SCALAR_DAMAGE = 0.75;

    @Override
    public Color getColor() {
        return new Color(115, 173, 255);
    }

    @Override
    public int castSpell(Coordinate targetLoc, Entity spellCaster, GameInstance gi, int magicPower) {
        if (gi.isSpaceAvailable(targetLoc, TagRegistry.NO_PATHING)){
            EntityStruct magicBombStruct = new EntityStruct(EntityRegistry.MAGIC_BOMB, "Magic Bomb", null);
            MagicBomb magicBomb = (MagicBomb)gi.instantiateEntity(magicBombStruct, targetLoc, gi.getCurrentLevel());
            magicBomb.setExplosionDamage(calculatePower(BASE_DAMAGE, magicPower, SCALAR_DAMAGE));
            magicBomb.onLevelEnter(gi);
            return calculateCooldown(BASE_COOLDOWN, magicPower);
        }
        else
            return 0;
    }

    @Override
    public int getDescriptionHeight() {
        return 5;
    }

    @Override
    public Layer drawDescription(Layer baseLayer, int magicPower) {
        //Draw flavor text
        baseLayer.inscribeString("Places a magic bomb\nthat explodes in 9\nturns.", 1, 1, InventoryPanel.FONT_WHITE, true);
        baseLayer.inscribeString(" ~~~ ", 1, 4, InventoryPanel.FONT_GRAY);
        //Draw base stats
        baseLayer.inscribeString(String.format("Damage   : %1$d", BASE_DAMAGE),   1, 5, InventoryPanel.FONT_WHITE);
        baseLayer.inscribeString(String.format("Cooldown : %1$d", BASE_COOLDOWN), 1, 6, InventoryPanel.FONT_WHITE);
        //Draw modifiers
        baseLayer.inscribeString(String.format("(+%1$d)", calculatePower(0, magicPower, SCALAR_DAMAGE)), 15, 5, InventoryPanel.FONT_BLUE);
        baseLayer.inscribeString(String.format("(-%1$d)", BASE_COOLDOWN - calculateCooldown(BASE_COOLDOWN, magicPower)),  15, 6, InventoryPanel.FONT_BLUE);
        return baseLayer;
    }

    @Override
    public Spell copy() {
        return new MagicBombSpell();
    }
}
