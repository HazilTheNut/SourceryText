package Game;

import Data.*;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Engine.ViewWindow;
import Game.AnimatedTiles.AnimatedTile;
import Game.Entities.Entity;
import Game.Registries.EntityRegistry;

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

    private String currentZoneName;

    private LayerManager lm;

    private long currentItemUID = 0;

    public GameInstance(LayerManager manager, ViewWindow window){

        lm = manager;

        FileIO io = new FileIO();

        player = new Player(window, manager, this);

        enterLevel(io.getRootFilePath() + "LevelData/gameStart.lda", new Coordinate(0, 0));

        window.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_F8)
                    DebugWindow.open();
            }
        });

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                ArrayList<AnimatedTile> animatedTiles = currentLevel.getAnimatedTiles();
                for (int i = 0; i < animatedTiles.size(); i++){
                    AnimatedTile animatedTile = animatedTiles.get(i);
                    SpecialText text = animatedTile.onDisplayUpdate();
                    currentLevel.getAnimatedTileLayer().editLayer(animatedTile.getLocation().getX(), animatedTile.getLocation().getY(), text);
                    if (text == null) {
                        currentLevel.removeAnimatedTile(animatedTile.getLocation());
                        i--;
                    }
                }
            }
        }, 50, 50);
    }

    void enterLevel(String levelFilePath, Coordinate playerPos){
        player.freeze();
        if (currentLevel != null) {
            currentLevel.onExit(lm);
            currentLevel.removeEntity(player);
        }
        String zoneName = getFilePathParentFolder(levelFilePath);
        if (currentZoneName == null || !currentZoneName.equals(zoneName)){
            currentZoneName = zoneName;
            levels.clear();
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

    private String getFilePathParentFolder(String path){
        int strEndLoc   = path.lastIndexOf('/');
        int strStartLoc = path.substring(0, strEndLoc-1).lastIndexOf('/');
        String output = path.substring(strStartLoc+1, strEndLoc);
        DebugWindow.reportf(DebugWindow.STAGE, "[GameInstance] Level folder name: %1$s memory: %2$d", output, levels.size());
        return output;
    }

    private void startLevel(Level level, Coordinate loc){
        level.addEntity(player);
        level.onEnter(lm);
        player.teleport(loc);
        player.updateCameraPos();
        player.unfreeze();
    }

    /**
     * Unpacks a level from storage, but doesn't set the current room to the newly unpacked one automatically.
     * @param levelFilePath The non-relative file path to the level's file
     * @return returns the loaded level
     */
    private Level loadLevel(String levelFilePath){
        DebugWindow.reportf(DebugWindow.STAGE, "[GameInstance.loadLevel] Begin level load to memory from %1$s", levelFilePath);
        Level newLevel = new Level(levelFilePath);

        FileIO io = new FileIO();

        DebugWindow.reportf(DebugWindow.STAGE, "[GameInstance.loadLevel] I/O Deserialization...");
        File levelFile = new File(levelFilePath);
        LevelData ldata = io.openLevel(levelFile);

        Layer backdrop = ldata.getBackdrop();
        backdrop.setImportance(LayerImportances.BACKDROP);
        newLevel.setBackdrop(backdrop);

        DebugWindow.reportf(DebugWindow.STAGE, "[GameInstance.loadLevel] Process entity data...");
        EntityStruct[][] entityMatrix = ldata.getEntityData();
        for (int col = 0; col < entityMatrix.length; col++){
            for (int row = 0; row < entityMatrix[0].length; row++){
                if (entityMatrix[col][row] != null){
                    EntityStruct savedStruct = entityMatrix[col][row];
                    EntityStruct struct = EntityRegistry.getEntityStruct(savedStruct.getEntityId());
                    struct.setArgs(savedStruct.getArgs());
                    struct.setItems(savedStruct.getItems());
                    Class entityClass = EntityRegistry.getEntityClass(struct.getEntityId());
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

        DebugWindow.reportf(DebugWindow.STAGE, "[GameInstance.loadLevel] Initialize tiles...");
        newLevel.intitializeTiles(ldata);

        DebugWindow.reportf(DebugWindow.STAGE, "[GameInstance.loadLevel] Assign warp zones...");
        newLevel.setWarpZones(ldata.getWarpZones());

        levels.add(newLevel);

        DebugWindow.reportf(DebugWindow.STAGE, "[GameInstance.loadLevel] Loading of level \'%1$s\' complete!", newLevel.getName());

        return newLevel;
    }

    public boolean isSpaceAvailable(Coordinate loc, int wallTag){
        return currentLevel.isLocationValid(loc) && getEntityAt(loc) == null && !getTileAt(loc).hasTag(wallTag);
    }

    public long issueItemUID(){
        currentItemUID++;
        return currentItemUID;
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

    public void addAnimatedTile(AnimatedTile animatedTile) { currentLevel.addAnimatedTile(animatedTile); }

    void doEnemyTurn(){
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

    public Entity getEntityAt(Coordinate loc){
        return currentLevel.getEntityAt(loc);
    }

    public Level getCurrentLevel() {
        return currentLevel;
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
            DebugWindow.reportf(DebugWindow.PERFORMANCE, "\nLevel \"%6$s\":\n\n > Size : %1$d x %2$d\n > Overlay Tiles : %3$d\n > Total Entities : %4$d\n > Animated Tiles : %5$d",
                    currentLevel.getBaseTiles().length, currentLevel.getBaseTiles()[0].length, currentLevel.getOverlayTiles().size(), currentLevel.getEntities().size(), currentLevel.getAnimatedTiles().size(), currentLevel.getName());
            ArrayList<Layer> layerStack = lm.getLayerStack();
            DebugWindow.reportf(DebugWindow.PERFORMANCE, "\nLayers: %1$d\n-------------------\n POS : PRI | NAME", layerStack.size());
            for (int i = 0; i < layerStack.size(); i++) {
                if (layerStack.get(i).getVisible())
                    DebugWindow.reportf(DebugWindow.PERFORMANCE, " %1$3d : %2$3d | %3$s", i, layerStack.get(i).getImportance(), layerStack.get(i).getName());
                else
                    DebugWindow.reportf(DebugWindow.PERFORMANCE, " %1$3d : %2$3d | (-) %3$s", i, layerStack.get(i).getImportance(), layerStack.get(i).getName());
            }
        }
    }

    private interface EntityOperation{
        void run();
    }
}
