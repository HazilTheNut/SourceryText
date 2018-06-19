package Game.Entities;

import Data.Coordinate;
import Data.EntityArg;
import Data.EntityStruct;
import Data.SerializationVersion;
import Engine.LayerManager;
import Game.GameInstance;
import Game.Player;

import java.util.ArrayList;

/**
 * Created by Jared on 5/11/2018.
 */
public class Sign extends Entity {

    /**
     * Sign:
     *
     * Upon interaction, it sends a message to the GameInstance's TextBox, which of course then displays it to the player.
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

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
