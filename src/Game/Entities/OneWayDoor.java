package Game.Entities;

import Data.Coordinate;
import Data.EntityArg;
import Data.EntityStruct;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.GameInstance;
import Game.Player;

import java.util.ArrayList;

/**
 * Created by Jared on 5/13/2018.
 */
public class OneWayDoor extends Entity {

    /**
     * OneWayDoor:
     *
     * Another kind of door, except OneWayDoor will refuse to open unless the player interacts with it from a specific side.
     *
     * OneWayDoor uses the '}' and '{' characters to denote direction, so currently left and right are the only possible directions.
     * Up and down are possibilities, but OnwWayDoors would not be easy to identify (as there are would be several different shapes to look for)
     */

    private boolean leftDirection = false; //Defines the direction of the OneWayDoor

    private final int DIR_ERROR = 0;
    private final int DIR_LEFT  = 1;
    private final int DIR_RIGHT = 2;
    private int direction = DIR_ERROR;

    @Override
    public void onInteract(Player player) {
        Coordinate diff = getLocation().subtract(player.getLocation()); //Get relative position to player
        if (direction != DIR_ERROR) {
            if ((direction == DIR_LEFT && diff.equals(new Coordinate(-1, 0))) || (direction == DIR_RIGHT && diff.equals(new Coordinate(1, 0))))
                selfDestruct();
            else
                gi.getTextBox().showMessage("The door is locked on the other side");
        } else {
            gi.getTextBox().showMessage("The door cannot be opened<p1>, ever");
        }
    }

    @Override
    public ArrayList<EntityArg> generateArgs() {
        ArrayList<EntityArg> args = super.generateArgs();
        args.add(new EntityArg("direction","\'right\' or \'left\'"));
        return args;
    }

    @Override
    public void initialize(Coordinate pos, LayerManager lm, EntityStruct entityStruct, GameInstance gameInstance) {
        super.initialize(pos, lm, entityStruct, gameInstance);
        String dir = readStrArg(searchForArg(entityStruct.getArgs(), "direction"), "ERROR");
        if (dir.toLowerCase().equals("left")) {
            direction = DIR_LEFT;
            getSprite().editLayer(0, 0, createIcon('{', entityStruct));
        } else if (dir.toLowerCase().equals("right")) {
            direction = DIR_RIGHT;
            getSprite().editLayer(0, 0, createIcon('}', entityStruct));
        }
    }

    private SpecialText createIcon(char c, EntityStruct entityStruct){
        return new SpecialText(c, entityStruct.getDisplayChar().getFgColor(), entityStruct.getDisplayChar().getBkgColor());
    }
}
