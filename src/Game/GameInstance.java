package Game;

import Data.*;
import Engine.Layer;
import Engine.LayerManager;
import Engine.ViewWindow;
import Game.Entities.Entity;
import Game.Registries.EntityRegistry;
import Game.Registries.TagRegistry;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Jared on 3/27/2018.
 */
public class GameInstance {

    private boolean isPlayerTurn = true;

    private Player player;
    private ArrayList<EntityOperation> entityOperations = new ArrayList<>();

    private Level currentLevel;
    private ArrayList<Level> levels = new ArrayList<>();

    private LayerManager lm;

    public GameInstance(LayerManager manager, ViewWindow window){

        lm = manager;

        FileIO io = new FileIO();

        player = new Player(window, manager, this);

        enterLevel(io.decodeFilePath(io.chooseLevel().getPath()), new Coordinate(40, 43));
    }

    public void enterLevel(String levelFilePath, Coordinate playerPos){
        if (currentLevel != null) {
            currentLevel.onExit(lm);
            currentLevel.removeEntity(player);
        }
        for (Level level : levels){
            if (level.getFilePath().equals(levelFilePath)){
                System.out.printf("[GameInstance.enterLevel] Level already found loaded at %1$s\n", levelFilePath);
                currentLevel = level;
                startLevel(currentLevel, playerPos);
                return;
            }
        }
        currentLevel = loadLevel(levelFilePath);
        startLevel(currentLevel, playerPos);
    }

    private void startLevel(Level level, Coordinate loc){
        level.addEntity(player);
        level.onEnter(lm);
        player.teleport(loc);
        player.updateCameraPos();
    }

    /**
     * Unpacks a level from storage, but doesn't set the current room to the newly unpacked one automatically.
     * @param levelFilePath The non-relative file path to the level's file
     * @return returns the loaded level
     */
    private Level loadLevel(String levelFilePath){
        Level newLevel = new Level(levelFilePath);

        FileIO io = new FileIO();

        File levelFile = new File(levelFilePath);
        LevelData ldata = io.openLevel(levelFile);

        Layer backdrop = ldata.getBackdrop();
        backdrop.setImportance(LayerImportances.BACKDROP);
        newLevel.setBackdrop(backdrop);

        EntityStruct[][] entityMatrix = ldata.getEntityData();
        EntityRegistry registry = new EntityRegistry();
        for (int col = 0; col < entityMatrix.length; col++){
            for (int row = 0; row < entityMatrix[0].length; row++){
                if (entityMatrix[col][row] != null){
                    EntityStruct savedStruct = entityMatrix[col][row];
                    EntityStruct struct = registry.getEntityStruct(savedStruct.getEntityId());
                    struct.setArgs(savedStruct.getArgs());
                    struct.setItems(savedStruct.getItems());
                    Class entityClass = registry.getEntityClass(struct.getEntityId());
                    try {
                        Entity e = (Entity)entityClass.newInstance();
                        e.initialize(new Coordinate(col, row), lm, struct, this);
                        newLevel.addEntity(e);
                    } catch (InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        newLevel.intitializeTiles(ldata);

        newLevel.setWarpZones(ldata.getWarpZones());

        levels.add(newLevel);

        return newLevel;
    }

    public boolean isSpaceAvailable(Coordinate loc){
        return currentLevel.isLocationValid(loc) && getEntityAt(loc) == null && !getTileAt(loc).hasTag(TagRegistry.TILE_WALL);
    }

    public Tile getTileAt(Coordinate loc){
        return currentLevel.getTileAt(loc);
    }

    public LayerManager getLayerManager() { return lm; }

    public Player getPlayer() { return player; }

    boolean isPlayerTurn() { return isPlayerTurn; }

    public Layer getBackdrop() {
        return currentLevel.getBackdrop();
    }

    public void removeEntity(Entity e){
        entityOperations.add(() -> currentLevel.removeEntity(e));
    }

    public void establishMouseInput(GameMouseInput mi){
        player.assignMouseInput(mi);
    }

    public void setPlayerTurn(boolean playerTurn) {
        isPlayerTurn = playerTurn;
    }

    void doEnemyTurn(){
        isPlayerTurn = false;
        EnemyTurnThread thread = new EnemyTurnThread();
        thread.start();
    }

    public Entity getEntityAt(Coordinate loc){
        return currentLevel.getEntityAt(loc);
    }

    public Level getCurrentLevel() {
        return currentLevel;
    }

    private class EnemyTurnThread extends Thread {

        @Override
        public void run() {
            for (EntityOperation op : entityOperations) {
                op.run();
            }
            for (Entity e : currentLevel.getEntities()) {
                e.onTurn();
            }
            for (Tile tile : currentLevel.getAllTiles()){
                tile.onTurn(GameInstance.this);
            }
            player.updateInventory();
            isPlayerTurn = true;
        }
    }

    private interface EntityOperation{
        void run();
    }
}
