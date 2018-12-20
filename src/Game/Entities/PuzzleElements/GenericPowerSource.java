package Game.Entities.PuzzleElements;

import Data.Coordinate;
import Data.EntityArg;
import Data.EntityStruct;
import Engine.LayerManager;
import Game.Entities.Entity;
import Game.GameInstance;

import java.util.ArrayList;

public class GenericPowerSource extends Entity {

    private ArrayList<Coordinate> powerToLocs;

    @Override
    public ArrayList<EntityArg> generateArgs() {
        ArrayList<EntityArg> args = super.generateArgs();
        args.add(new EntityArg("powerTo", "[0,0],[0,0],..."));
        return args;
    }

    @Override
    public void initialize(Coordinate pos, LayerManager lm, EntityStruct entityStruct, GameInstance gameInstance) {
        super.initialize(pos, lm, entityStruct, gameInstance);
        powerToLocs = readCoordListArg(searchForArg(entityStruct.getArgs(), "powerTo"));
    }

    void powerOn(){
        for (Coordinate powerTo : powerToLocs) {
            ArrayList<Entity> entities = gi.getCurrentLevel().getEntitiesAt(powerTo);
            for (Entity e : entities) {
                if (e instanceof Powerable) {
                    Powerable powerable = (Powerable) e;
                    powerable.onPowerOn();
                }
            }
        }
    }

    void powerOff(){
        for (Coordinate powerTo : powerToLocs) {
            ArrayList<Entity> entities = gi.getCurrentLevel().getEntitiesAt(powerTo);
            for (Entity e : entities) {
                if (e instanceof Powerable) {
                    Powerable powerable = (Powerable) e;
                    powerable.onPowerOff();
                }
            }
        }
    }
}
