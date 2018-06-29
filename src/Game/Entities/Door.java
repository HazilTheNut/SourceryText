package Game.Entities;

import Data.Coordinate;
import Data.EntityArg;
import Data.EntityStruct;
import Data.SerializationVersion;
import Engine.LayerManager;
import Game.Entities.PuzzleElements.Powerable;
import Game.GameInstance;
import Game.Player;

import java.util.ArrayList;

/**
 * Created by Jared on 5/13/2018.
 */
public class Door extends Entity {

    /**
     * Door:
     *
     * An Entity that acts as a solid wall until interacted with. Additionally, it can power other elements upon opening.
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private ArrayList<Coordinate> powerToLocs;

    @Override
    public ArrayList<EntityArg> generateArgs() {
        ArrayList<EntityArg> args = super.generateArgs();
        args.add(new EntityArg("powerTo","[0,0],[0,0],...."));
        return args;
    }

    @Override
    public void initialize(Coordinate pos, LayerManager lm, EntityStruct entityStruct, GameInstance gameInstance) {
        super.initialize(pos, lm, entityStruct, gameInstance);
        powerToLocs = readCoordListArg(searchForArg(entityStruct.getArgs(), "powerTo"));
    }

    @Override
    public void onInteract(Player player) {
        for (Coordinate loc : powerToLocs){
            for (Entity e : gi.getCurrentLevel().getEntitiesAt(loc)){
                if (e instanceof Powerable) {
                    Powerable powerable = (Powerable) e;
                    powerable.onPowerOn();
                }
            }
        }
        selfDestruct();
    }
}
