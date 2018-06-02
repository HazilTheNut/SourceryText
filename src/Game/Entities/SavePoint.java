package Game.Entities;

import Data.EntityArg;
import Game.Player;

import java.util.ArrayList;

public class SavePoint extends Entity {

    /**
     * SavePoint:
     *
     * Upon interaction, SavePoint opens the "Save Game" menu.
     */

    @Override
    public void onInteract(Player player) {
        gi.getGameMaster().openGameSaveMenu();
    }

    @Override
    public ArrayList<EntityArg> generateArgs() {
        return new ArrayList<>();
    }
}
