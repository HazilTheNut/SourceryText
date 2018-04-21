package Game.Spells;

import Data.Coordinate;
import Game.Entities.Entity;
import Game.GameInstance;

/**
 * Created by Jared on 4/19/2018.
 */
public class Spell {

    /**
     * Perfroms spell cast
     * @param targetLoc Target location for spell (aiming, etc.)
     * @param spellCaster The one doing the spell casting
     * @param gi The Game Instance
     * @return The spell's cooldown
     */
    public int castSpell(Coordinate targetLoc, Entity spellCaster, GameInstance gi){ return 0; }

    public String getName(){ return "~";}

}
