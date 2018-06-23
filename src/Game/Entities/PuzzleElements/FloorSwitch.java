package Game.Entities.PuzzleElements;

import Data.Coordinate;
import Data.EntityArg;
import Data.EntityStruct;
import Data.SerializationVersion;
import Engine.LayerManager;
import Game.Entities.Entity;
import Game.GameInstance;

import java.util.ArrayList;

public class FloorSwitch extends Entity {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private ArrayList<Coordinate> powerToLocs;
    private boolean isPowering;

    @Override
    public ArrayList<EntityArg> generateArgs() {
        ArrayList<EntityArg> args = super.generateArgs();
        args.add(new EntityArg("powerToLocs", "[0,0],[0,0],..."));
        return args;
    }

    @Override
    public void initialize(Coordinate pos, LayerManager lm, EntityStruct entityStruct, GameInstance gameInstance) {
        super.initialize(pos, lm, entityStruct, gameInstance);
        powerToLocs = readCoordListArg(searchForArg(entityStruct.getArgs(), "powerToLocs"));
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public void onTurn() {
        if (gi.getCurrentLevel().getSolidEntityAt(getLocation()) != null){ //Entity standing on the button
            if (!isPowering){
                powerOn();
                isPowering = true;
            }
        } else { //Entities are not standing on this button
            if (isPowering){
                powerOff();
                isPowering = false;
            }
        }
    }

    private void powerOn(){
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

    private void powerOff(){
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
