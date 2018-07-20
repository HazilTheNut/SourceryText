package Game.Spells;

import Data.Coordinate;
import Data.EntityStruct;
import Data.SerializationVersion;
import Game.Entities.Entity;
import Game.Entities.MagicBomb;
import Game.GameInstance;
import Game.Registries.EntityRegistry;
import Game.Registries.TagRegistry;

public class MagicBombSpell extends Spell {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public String getName() {
        return "Magic Bomb";
    }

    @Override
    public int castSpell(Coordinate targetLoc, Entity spellCaster, GameInstance gi, int magicPower) {
        if (gi.isSpaceAvailable(targetLoc, TagRegistry.NO_PATHING)){
            EntityStruct magicBombStruct = new EntityStruct(EntityRegistry.MAGIC_BOMB, "Magic Bomb", null);
            MagicBomb magicBomb = (MagicBomb)gi.instantiateEntity(magicBombStruct, targetLoc, gi.getCurrentLevel());
            magicBomb.setExplosionDamage(calculateDamage(15, magicPower));
            magicBomb.onLevelEnter(gi);
            return calculateCooldown(20, magicPower);
        }
        else
            return 0;
    }

    @Override
    public Spell copy() {
        return new MagicBombSpell();
    }
}
