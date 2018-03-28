package Game;

import Data.EntityStruct;
import Data.FileIO;
import Data.LevelData;
import Engine.LayerManager;
import Engine.ViewWindow;
import Game.Entities.Entity;
import Game.Registries.EntityRegistry;

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

        EntityStruct[][] entityMatrix = ldata.getEntityData();
        EntityRegistry registry = new EntityRegistry();
        for (int col = 0; col < entityMatrix.length; col++){
            for (int row = 0; row < entityMatrix[0].length; row++){
                EntityStruct struct = entityMatrix[col][row];
                if (struct != null){
                    Class entityClass = registry.getEntityClass(struct.getEntityId());
                    try {
                        Entity e = (Entity)entityClass.newInstance();
                        e.initialize(new Coordinate(col, row), manager, struct, this);
                        entities.add(e);
                    } catch (InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        new Player(window, manager, this);
    }

    boolean isPlayerTurn() { return isPlayerTurn; }

    void doEnemyTurn(){
        isPlayerTurn = false;
        EnemyTurnThread thread = new EnemyTurnThread();
        thread.start();
    }

    private class EnemyTurnThread extends Thread {

        @Override
        public void run() {
            for (Entity e : entities) {
                e.onTurn();
            }
            isPlayerTurn = true;
        }
    }
}
