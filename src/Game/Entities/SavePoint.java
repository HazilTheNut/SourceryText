package Game.Entities;

import Data.EntityArg;
import Data.SerializationVersion;
import Game.Player;

import java.util.ArrayList;

public class SavePoint extends Entity {

    /**
     * SavePoint:
     *
     * Upon interaction, SavePoint opens the "Save Game" menu.
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onInteract(Player player) {
        gi.getGameMaster().openGameSaveMenu();
    }

    @Override
    public ArrayList<EntityArg> generateArgs() {
        return new ArrayList<>();
    }
}
