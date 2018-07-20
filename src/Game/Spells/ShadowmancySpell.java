package Game.Spells;

import Data.Coordinate;
import Data.EntityStruct;
import Data.SerializationVersion;
import Game.Entities.Entity;
import Game.GameInstance;
import Game.Player;
import Game.PlayerShadow;
import Game.Registries.EntityRegistry;

public class ShadowmancySpell extends Spell {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public String getName() {
        return "Shadowmancy";
    }

    @Override
    public int castSpell(Coordinate targetLoc, Entity spellCaster, GameInstance gi, int magicPower) {
        if (spellCaster instanceof Player){
            PlayerShadow ps = (PlayerShadow)getPlayerShadow(gi);
            if (ps == null) {
                EntityStruct shadowStruct = new EntityStruct(EntityRegistry.PLAYER_SHADOW, "Shadow", null);
                PlayerShadow playerShadow = (PlayerShadow) gi.instantiateEntity(shadowStruct, targetLoc, gi.getCurrentLevel());
                playerShadow.onLevelEnter();
                return calculateCooldown(18, magicPower);
            } else {
                ps.setOffset(targetLoc.subtract(spellCaster.getLocation()));
                return calculateCooldown(10, magicPower);
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
    public Spell copy() {
        return new ShadowmancySpell();
    }
}
