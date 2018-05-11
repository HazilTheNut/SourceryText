package Game.Entities;

import Data.Coordinate;
import Data.EntityArg;
import Data.EntityStruct;
import Engine.LayerManager;
import Game.GameInstance;
import Game.Player;

import java.util.ArrayList;

/**
 * Created by Jared on 5/11/2018.
 */
public class Sign extends Entity {

    private String text;

    @Override
    public ArrayList<EntityArg> generateArgs() {
        ArrayList<EntityArg> args = super.generateArgs();
        args.add(new EntityArg("text", ""));
        return args;
    }

    @Override
    public void onInteract(Player player) {
        if (!text.equals("")){
            gi.getTextBox().showMessage(text);
        }
    }

    @Override
    public void initialize(Coordinate pos, LayerManager lm, EntityStruct entityStruct, GameInstance gameInstance) {
        super.initialize(pos, lm, entityStruct, gameInstance);
        text = readStrArg(searchForArg(entityStruct.getArgs(), "text"), "");
    }
}
