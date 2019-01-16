package Game.Entities.PuzzleElements;

import Data.Coordinate;
import Data.EntityArg;
import Data.EntityStruct;
import Data.SerializationVersion;
import Engine.LayerManager;
import Game.Entities.CombatEntity;
import Game.GameInstance;
import Game.Player;
import Game.Registries.TagRegistry;

import java.util.ArrayList;

public class MovableCrate extends CombatEntity {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private int fireTimer;

    @Override
    public void initialize(Coordinate pos, LayerManager lm, EntityStruct entityStruct, GameInstance gameInstance) {
        super.initialize(pos, lm, entityStruct, gameInstance);
        fireTimer = 5;
    }

    @Override
    public ArrayList<EntityArg> generateArgs() {
        ArrayList<EntityArg> args = super.generateArgs();
        EntityArg hpArg = searchForArg(args, "maxHealth");
        if (hpArg != null) {
            hpArg.setArgValue("100");
        }
        return args;
    }

    @Override
    public void onTurn() {
        super.onTurn();
        if (hasTag(TagRegistry.ON_FIRE)){
            fireTimer--;
            setHealth(getMaxHealth() / 5 * fireTimer);
        }
        if (fireTimer == 0){
            selfDestruct();
        }
    }

    @Override
    public void onInteract(Player player) {
        Coordinate moveVector = getLocation().subtract(player.getLocation());
        move(moveVector.getX(), moveVector.getY());
    }
}
