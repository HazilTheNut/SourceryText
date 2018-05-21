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

    private boolean leftDirection = false;

    @Override
    public void onInteract(Player player) {
        Coordinate diff = getLocation().subtract(player.getLocation());
        if ((leftDirection && diff.equals(new Coordinate(-1, 0))) || (!leftDirection && diff.equals(new Coordinate(1, 0))))
            selfDestruct();
        else
            gi.getTextBox().showMessage("The door is locked on the other side");
    }

    @Override
    public ArrayList<EntityArg> generateArgs() {
        ArrayList<EntityArg> args = super.generateArgs();
        args.add(new EntityArg("direction","right"));
        return args;
    }

    @Override
    public void initialize(Coordinate pos, LayerManager lm, EntityStruct entityStruct, GameInstance gameInstance) {
        super.initialize(pos, lm, entityStruct, gameInstance);
        String dir = readStrArg(searchForArg(entityStruct.getArgs(), "direction"), "right");
        if (dir.toLowerCase().equals("left")) {
            leftDirection = true;
            getSprite().editLayer(0, 0, new SpecialText('{', entityStruct.getDisplayChar().getFgColor(), entityStruct.getDisplayChar().getBkgColor()));
        }
    }
}
