package Game.Spells;

import Data.Coordinate;
import Data.EntityStruct;
import Data.SerializationVersion;
import Game.Entities.Entity;
import Game.GameInstance;
import Game.Player;
import Game.PlayerShadow;

public class ShadowmancySpell extends Spell {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public String getName() {
        return "Shadowmancy";
    }

    @Override
    public int castSpell(Coordinate targetLoc, Entity spellCaster, GameInstance gi, int magicPower) {
        if (spellCaster instanceof Player){
            EntityStruct shadowStruct = new EntityStruct(10, "Shadow", null);
            PlayerShadow playerShadow = (PlayerShadow)gi.instantiateEntity(shadowStruct, targetLoc, gi.getCurrentLevel());
            playerShadow.setOffset(targetLoc.subtract(spellCaster.getLocation()));
            playerShadow.onLevelEnter();
            return 4;
        }
        return 0;
    }
}
