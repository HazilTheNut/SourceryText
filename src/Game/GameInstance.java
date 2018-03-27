package Game;

import Data.FileIO;
import Data.LevelData;
import Engine.Layer;
import Engine.LayerManager;
import Engine.ViewWindow;
import Game.Entities.Entity;
import Game.Entities.FallingTestEntity;

import java.util.ArrayList;

/**
 * Created by Jared on 3/27/2018.
 */
public class GameInstance {

    private boolean isPlayerTurn = true;

    private ArrayList<Entity> entities = new ArrayList<>();

    public GameInstance(LayerManager manager, ViewWindow window){

        FileIO io = new FileIO();

        LevelData ldata = io.openLevel(io.chooseLevel());

        manager.addLayer(ldata.getBackdrop());

        entities.add(new FallingTestEntity(new Coordinate(5, 0), manager, "FallingEntity"));

        new Player(window, manager, this);
    }

    boolean isPlayerTurn() { return isPlayerTurn; }

    public void doEnemyTurn(){
        isPlayerTurn = false;
        for (Entity e : entities) e.onTurn();
        isPlayerTurn = true;
    }
}
