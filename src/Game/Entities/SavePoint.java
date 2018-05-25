package Game.Entities;

import Data.EntityArg;
import Data.FileIO;
import Game.Player;

import java.util.ArrayList;

public class SavePoint extends Entity {

    @Override
    public void onInteract(Player player) {
        FileIO io = new FileIO();
        io.serializeGameInstance(gi, io.getRootFilePath());
    }

    @Override
    public ArrayList<EntityArg> generateArgs() {
        return new ArrayList<>();
    }
}
