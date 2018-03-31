package Game;

import Data.EntityStruct;
import Data.FileIO;
import Data.LayerImportances;
import Data.LevelData;
import Engine.Layer;
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

    private Layer backdrop;
    private Player player;
    private ArrayList<Entity> entities = new ArrayList<>();
    private ArrayList<EntityOperation> entityOperations = new ArrayList<>();

    public GameInstance(LayerManager manager, ViewWindow window){

        FileIO io = new FileIO();

        LevelData ldata = io.openLevel(io.chooseLevel());

        backdrop = ldata.getBackdrop();
        backdrop.setImportance(LayerImportances.BACKDROP);
        manager.addLayer(backdrop);

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

        player = new Player(window, manager, this);
    }

    public Player getPlayer() { return player; }

    boolean isPlayerTurn() { return isPlayerTurn; }

    public Layer getBackdrop() {
        return backdrop;
    }

    public void removeEntity(Entity e){
        entityOperations.add(() -> entities.remove(e));
    }

    public void establishMouseInput(GameMouseInput mi){
        player.assignMouseInput(mi);
    }

    void doEnemyTurn(){
        isPlayerTurn = false;
        EnemyTurnThread thread = new EnemyTurnThread();
        thread.start();
    }

    public Entity getEntityAt(Coordinate loc){
        for (Entity e : entities){
            if (e.getLocation().equals(loc)) return e;
        }
        return null;
    }

    private class EnemyTurnThread extends Thread {

        @Override
        public void run() {
            for (EntityOperation op : entityOperations)
                op.run();
            for (Entity e : entities) {
                e.onTurn();
            }
            isPlayerTurn = true;
        }
    }

    private interface EntityOperation{
        void run();
    }
}
