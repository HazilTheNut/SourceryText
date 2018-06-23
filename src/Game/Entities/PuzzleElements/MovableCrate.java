package Game.Entities.PuzzleElements;

import Data.Coordinate;
import Data.EntityStruct;
import Data.SerializationVersion;
import Engine.LayerManager;
import Game.Entities.CombatEntity;
import Game.GameInstance;
import Game.Player;
import Game.Registries.TagRegistry;

public class MovableCrate extends CombatEntity {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private int fireTimer;

    @Override
    public void initialize(Coordinate pos, LayerManager lm, EntityStruct entityStruct, GameInstance gameInstance) {
        super.initialize(pos, lm, entityStruct, gameInstance);
        setMaxHealth(100);
        fireTimer = 5;
    }

    @Override
    public void onTurn() {
        super.onTurn();
        if (hasTag(TagRegistry.ON_FIRE)){
            fireTimer--;
            setHealth(getHealth() - 20);
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
