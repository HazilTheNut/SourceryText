package Game;

import Data.*;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Engine.ViewWindow;
import Game.AnimatedTiles.AnimatedTile;
import Game.Entities.Entity;
import Game.Registries.EntityRegistry;
import Game.Registries.TagRegistry;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Jared on 3/27/2018.
 */
public class GameInstance {

    private boolean isPlayerTurn = true;

    private Player player;
    private ArrayList<EntityOperation> entityOperations = new ArrayList<>();

    private Level currentLevel;
    private ArrayList<Level> levels = new ArrayList<>();

    private ArrayList<AnimatedTile> animatedTiles = new ArrayList<>();

    private LayerManager lm;

    public GameInstance(LayerManager manager, ViewWindow window){

        lm = manager;

        FileIO io = new FileIO();

        player = new Player(window, manager, this);

        enterLevel(io.decodeFilePath(io.chooseLevel().getPath()), new Coordinate(40, 43));

        window.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_F8)
                    DebugWindow.open();
            }
        });

        Layer backdrop = currentLevel.getBackdrop();
        Layer animationLayer = new Layer(backdrop.getCols(), backdrop.getRows(), "tile_animation", 0, 0, LayerImportances.TILE_ANIM);
        lm.addLayer(animationLayer);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for (int i = 0; i < animatedTiles.size(); i++){
                    AnimatedTile animatedTile = animatedTiles.get(i);
                    SpecialText text = animatedTile.onDisplayUpdate();
                    animationLayer.editLayer(animatedTile.getLocation().getX(), animatedTile.getLocation().getY(), text);
                    if (text == null) {
                        animatedTiles.remove(i);
                        i--;
                    }
                }
            }
        }, 50, 50);
    }

    public void enterLevel(String levelFilePath, Coordinate playerPos){
        if (currentLevel != null) {
            currentLevel.onExit(lm);
            currentLevel.removeEntity(player);
        }
        for (Level level : levels){
            if (level.getFilePath().equals(levelFilePath)){
                DebugWindow.reportf(DebugWindow.STAGE, "[GameInstance.enterLevel] Level already found loaded at %1$s", levelFilePath);
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

    public void addAnimatedTile(AnimatedTile animatedTile) { animatedTiles.add(animatedTile); }

    Thread doEnemyTurn(){
        isPlayerTurn = false;
        EnemyTurnThread thread = new EnemyTurnThread();
        thread.start();
        return thread;
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
            long[] runTimes = new long[4];
            runTimes[0] = System.nanoTime();
            for (EntityOperation op : entityOperations) {
                op.run();
            }
            runTimes[1] = System.nanoTime();
            for (Entity e : currentLevel.getEntities()) {
                e.onTurn();
            }
            runTimes[2] = System.nanoTime();
            for (Tile tile : currentLevel.getAllTiles()){
                tile.onTurn(GameInstance.this);
            }
            runTimes[3] = System.nanoTime();
            reportUpdatePerformance(runTimes);
            isPlayerTurn = true;
        }
    }

    private void reportUpdatePerformance(long[] times){
        if (times.length >= 4) {
            double entityop   = (double)(times[1] - times[0]) / 1000000;
            double entityturn = (double)(times[2] - times[1]) / 1000000;
            double tileupdate = (double)(times[3] - times[2]) / 1000000;
            double total      = (double)(times[3] - times[0]) / 1000000;
            DebugWindow.clear(DebugWindow.PERFORMANCE);
            DebugWindow.reportf(DebugWindow.PERFORMANCE, "[GameInstance] Turn Update Results:\n>  entityop:   %1$fms\n>  entityturn: %2$fms\n>  tileupdate: %3$fms\n\n>   TOTAL: %4$fms", entityop, entityturn, tileupdate, total);
            DebugWindow.reportf(DebugWindow.PERFORMANCE, "\n[GameInstance] Previous Draw Time:\n>  %1$fms", (double)lm.getPreviousCompileTime() / 1000000);
        }
    }

    private interface EntityOperation{
        void run();
    }
}
