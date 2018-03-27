package Game;

import Data.FileIO;
import Data.LevelData;
import Engine.Layer;
import Engine.LayerManager;
import Engine.ViewWindow;

/**
 * Created by Jared on 3/27/2018.
 */
public class GameInstance {

    public GameInstance(LayerManager manager, ViewWindow window){

        FileIO io = new FileIO();

        LevelData ldata = io.openLevel(io.chooseLevel());

        manager.addLayer(ldata.getBackdrop());

        new Player(window, manager);
    }

}
