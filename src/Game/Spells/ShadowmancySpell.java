package Game.Spells;

import Data.Coordinate;
import Data.EntityStruct;
import Data.SerializationVersion;
import Engine.Layer;
import Game.Entities.Entity;
import Game.GameInstance;
import Game.Player;
import Game.PlayerShadow;
import Game.Registries.EntityRegistry;
import Game.UI.InventoryPanel;

import java.awt.*;

public class ShadowmancySpell extends Spell {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public String getName() {
        return "Shadowmancy";
    }

    private static final int BASE_COOLDOWN = 18;

    @Override
    public Color getColor() {
        return new Color(179, 204, 173);
    }

    @Override
    public int castSpell(Coordinate targetLoc, Entity spellCaster, GameInstance gi, int magicPower) {
        if (spellCaster instanceof Player){
            PlayerShadow ps = (PlayerShadow)getPlayerShadow(gi);
            if (ps == null) {
                EntityStruct shadowStruct = new EntityStruct(EntityRegistry.PLAYER_SHADOW, "Shadow", null);
                gi.instantiateEntity(shadowStruct, targetLoc, gi.getCurrentLevel());
                return calculateCooldown(BASE_COOLDOWN, magicPower);
            } else {
                ps.setOffset(targetLoc.subtract(spellCaster.getLocation()));
                return calculateCooldown(BASE_COOLDOWN / 2, magicPower);
            }
        }
        return 0;
    }

    private Entity getPlayerShadow(GameInstance gi){
        for (Entity e : gi.getCurrentLevel().getEntities()){
            if (e.getName().equals(EntityRegistry.getEntityStruct(EntityRegistry.PLAYER_SHADOW).getEntityName()))
                return e;
        }
        return null;
    }

    @Override
    public int getDescriptionHeight() {
        return 4;
    }

    @Override
    public Layer drawDescription(Layer baseLayer, int magicPower) {
        //Draw flavor text
        baseLayer.inscribeString("Project your shadow\nthat will copy your\nactions", 1, 1, InventoryPanel.FONT_WHITE, true);
        baseLayer.inscribeString(" ~~~ ", 1, 4, InventoryPanel.FONT_GRAY);
        //Draw base stats
        baseLayer.inscribeString(String.format("Cooldown : %1$d", BASE_COOLDOWN), 1, 5, InventoryPanel.FONT_WHITE);
        //Draw modifiers
        baseLayer.inscribeString(String.format("(-%1$d)", BASE_COOLDOWN - calculateCooldown(BASE_COOLDOWN, magicPower)),  15, 5, InventoryPanel.FONT_BLUE);
        return baseLayer;
    }

    @Override
    public Spell copy() {
        return new ShadowmancySpell();
    }
}
