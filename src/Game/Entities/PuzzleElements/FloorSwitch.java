package Game.Entities.PuzzleElements;

import Data.Coordinate;
import Data.EntityArg;
import Data.EntityStruct;
import Data.SerializationVersion;
import Engine.LayerManager;
import Game.Entities.Entity;
import Game.GameInstance;

import java.util.ArrayList;

public class FloorSwitch extends GenericPowerSource {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private boolean isPowering;

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public void onTurn() {
        if (gi.getCurrentLevel().getSolidEntityAt(getLocation()) != null){ //Entity standing on the button
            if (!isPowering){ //Only check when pushed down or lifted up, or otherwise known as any change of state
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


}
