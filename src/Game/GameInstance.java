package Game;

import Data.*;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.AnimatedTiles.AnimatedTile;
import Game.Debug.DebugWindow;
import Game.Entities.Entity;
import Game.Registries.EntityRegistry;
import Game.Registries.TagRegistry;

import java.awt.*;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Jared on 3/27/2018.
 */
public class GameInstance implements Serializable {

    /**
     * GameInstance:
     *
     * Manages a running game of SourceryText, containing all the necessary fields to operate it.
     * It's also serializable, so it doubles as a saved game too.
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private boolean isPlayerTurn = true;

    private ArrayList<EntityOperation> entityOperations;

    private Player player; //Makes the game interactive

    private transient GameMouseInput mi; //Makes interaction happen
    private transient GameMaster gameMaster;

    private Level currentLevel;
    private ArrayList<Level> levels;

    private String currentZoneName; //Name of the Zone. Used to check if a Level being loaded is still in the same Zone or not.

    private transient LayerManager lm;
    private transient TextBox textBox;
    private transient QuickMenu quickMenu;

    private transient Timer tileAnimationTimer;

    private long currentUID = 1;
    private long turnCounter = 0;

    public GameInstance(){
        levels = new ArrayList<>();
        entityOperations = new ArrayList<>();
    }

    void assignLayerManager(LayerManager lm){
        this.lm = lm;
    }

    void assignMouseInput(GameMouseInput gmi){
        mi = gmi;
    }

    void assignGameMaster(GameMaster master) {
        gameMaster = master;
    }

    /**
     * Sets up the GameInstance
     */
    void initialize(){
        entityOperations = new ArrayList<>();

        if (player == null) {
            player = new Player(this);
        }
        player.playerInit();

        startAnimations();

        textBox = new TextBox(lm, getPlayer());
        quickMenu = new QuickMenu(lm, getPlayer());

        pathTestLayer = new Layer(0, 0, "pathtest", 0, 0, LayerImportances.VFX);
        pathTestLayer.setVisible(false);
        lm.addLayer(pathTestLayer);
    }

    void stopAnimations(){
        tileAnimationTimer.cancel();
    }

    /**
     * Starts the timer that updates the AnimatedTiles.
     */
    void startAnimations(){
        tileAnimationTimer = new Timer();
        tileAnimationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (currentLevel != null) {
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
            }
        }, 50, 50);
    }

    /**
     * Closes the GameInstance, preparing everything to be garbage-collected.
     */
    void dispose(){
        tileAnimationTimer.cancel();
        if (currentLevel != null) {
            currentLevel.onExit(lm);
            currentLevel.removeEntity(getPlayer());
            //currentLevel.destroy();
        }
        currentLevel = null;
        player.cleanup();
        player = null;
        textBox.setPlayer(null);
        quickMenu.setPlayer(null);
        quickMenu.clearMenu();
    }

    /**
     * Switches to a new Level, and loads one into memory if it doesn't exist in the list of loaded levels yet.
     *
     * @param levelFilePath The full (non-relative) file path to the .lda file being entered into.
     * @param playerPos The player's new position to be in.
     */
    void enterLevel(String levelFilePath, Coordinate playerPos){
        getPlayer().freeze();
        if (currentLevel != null) {
            currentLevel.onExit(lm);
            currentLevel.removeEntity(getPlayer());
        }
        String zoneName = getFilePathParentFolder(levelFilePath);
        if (currentZoneName == null || !currentZoneName.equals(zoneName)){
            currentZoneName = zoneName;
            levels.clear();
        }
        for (Level level : levels){
            if (level.getFilePath().equals(levelFilePath)){
                DebugWindow.reportf(DebugWindow.STAGE, "GameInstance.enterLevel","Level already found loaded at %1$s", levelFilePath);
                currentLevel = level;
                startLevel(currentLevel, playerPos);
                return;
            }
        }
        currentLevel = loadLevel(levelFilePath);
        startLevel(currentLevel, playerPos);
    }

    /**
     * Gets the name of the containing folder of a file.
     *
     * @param path The file path of the file
     * @return Name of containing folder
     */
    private String getFilePathParentFolder(String path){
        int strEndLoc   = path.lastIndexOf('/');
        int strStartLoc = path.substring(0, strEndLoc-1).lastIndexOf('/');
        String output = path.substring(strStartLoc+1, strEndLoc);
        DebugWindow.reportf(DebugWindow.STAGE, "GameInstance.getFilePathParentFolder","Level folder name: %1$s memory: %2$d", output, levels.size());
        return output;
    }

    private void startLevel(Level level, Coordinate loc){
        level.addEntity(getPlayer());
        level.onEnter(lm);
        getPlayer().setPos(loc);
        getPlayer().updateCameraPos();
        getPlayer().unfreeze();
    }

    /**
     * Unpacks a level from storage, but doesn't set the current room to the newly unpacked one automatically.
     *
     * @param levelFilePath The non-relative file path to the level's file
     * @return returns the loaded level
     */
    private Level loadLevel(String levelFilePath){
        DebugWindow.reportf(DebugWindow.STAGE, "GameInstance.loadLevel", "Begin level load to memory from %1$s", levelFilePath);
        Level newLevel = new Level(levelFilePath);

        FileIO io = new FileIO();

        DebugWindow.reportf(DebugWindow.STAGE, "GameInstance.loadLevel", "I/O Deserialization...");
        File levelFile = new File(levelFilePath);
        LevelData ldata = io.openLevel(levelFile);

        DebugWindow.reportf(DebugWindow.STAGE, "GameInstance.loadLevel", "Initialize level...");
        newLevel.initialize(ldata);

        DebugWindow.reportf(DebugWindow.STAGE, "GameInstance.loadLevel", "Process entity data...");
        EntityStruct[][] entityMatrix = ldata.getEntityData();
        for (int col = 0; col < entityMatrix.length; col++){
            for (int row = 0; row < entityMatrix[0].length; row++){
                if (entityMatrix[col][row] != null){
                    instantiateEntity(entityMatrix[col][row], new Coordinate(col, row), newLevel);
                }
            }
        }

        DebugWindow.reportf(DebugWindow.STAGE, "GameInstance.loadLevel", "Assign warp zones...");
        newLevel.setWarpZones(ldata.getWarpZones());

        levels.add(newLevel);

        DebugWindow.reportf(DebugWindow.STAGE, "GameInstance.loadLevel", "Loading of level \'%1$s\' complete!", newLevel.getName());

        return newLevel;
    }

    /**
     * Instantiates an entity into a level, running initialize() and retrieving data from EntityRegistry.
     *
     * @param base The base EntityStruct to build the Entity from
     * @param pos The pos where the entity should wind up
     * @param level The level being added to
     * @return The new Entity
     */
    public Entity instantiateEntity(EntityStruct base, Coordinate pos, Level level){
        Entity e = null;
        EntityStruct fromRegistry = EntityRegistry.getEntityStruct(base.getEntityId());
        fromRegistry.setArgs(base.getArgs());
        fromRegistry.setItems(base.getItems());
        Class entityClass = EntityRegistry.getEntityClass(fromRegistry.getEntityId());
        try {
            e = (Entity)entityClass.newInstance();
            e.initialize(pos.copy(), lm, fromRegistry, this);
            level.addEntity(e);
        } catch (InstantiationException | IllegalAccessException er) {
            er.printStackTrace();
        }
        return e;
    }

    public boolean isSpaceAvailable(Coordinate loc, int wallTag){
        return currentLevel.isLocationValid(loc) && currentLevel.getSolidEntityAt(loc) == null && !getTileAt(loc).hasTag(wallTag);
    }

    public long issueUID(){
        currentUID++;
        DebugWindow.reportf(DebugWindow.STAGE, "GameInstance.issueUID","Current UID: %1$d", currentUID);
        return currentUID;
    }

    public Tile getTileAt(Coordinate loc){
        return currentLevel.getTileAt(loc);
    }

    public LayerManager getLayerManager() { return lm; }

    public Player getPlayer() { return player; }

    public GameMaster getGameMaster() {
        return gameMaster;
    }

    boolean isPlayerTurn() { return isPlayerTurn; }

    public Layer getBackdrop() {
        return currentLevel.getBackdrop();
    }

    public void removeEntity(Entity e){
        entityOperations.add(() -> {
            currentLevel.removeEntity(e);
        });
    }

    public void establishMouseInput(){
        mi.addInputReceiver(textBox);
        mi.addInputReceiver(quickMenu);
        getPlayer().assignMouseInput(mi);
    }

    public void setPlayerTurn(boolean playerTurn) {
        isPlayerTurn = playerTurn;
    }

    public void addAnimatedTile(AnimatedTile animatedTile) { currentLevel.addAnimatedTile(animatedTile); }

    /**
     * Operates the turn for the enemies.
     * Updates Tiles afterwards too.
     */
    void doEnemyTurn(){
        //Thread enemyTurnThread = new Thread(() -> {
            long[] runTimes = new long[4];
            runTimes[0] = System.nanoTime();
            for (EntityOperation op : entityOperations) {
                op.run();
            }
            entityOperations.clear();
            calculatePathing();
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
            getPlayer().updateHUD();
            getPlayer().updateSynopsis();
            turnCounter++;
            DebugWindow.reportf(DebugWindow.STAGE, "GameInstace:turnCounter", "%1$d", turnCounter);
            isPlayerTurn = true;
        //});
        //enemyTurnThread.start();
    }

    public Level getCurrentLevel() {
        return currentLevel;
    }

    public String getCurrentZoneName() {
        return currentZoneName;
    }

    private void reportUpdatePerformance(long[] times){
        if (times.length >= 4) {
            double entityop   = (double)(times[1] - times[0]) / 1000000;
            double entityturn = (double)(times[2] - times[1]) / 1000000;
            double tileupdate = (double)(times[3] - times[2]) / 1000000;
            double total      = (double)(times[3] - times[0]) / 1000000;
            DebugWindow.reportf(DebugWindow.PERFORMANCE, "GameInstance.reportUpdatePerformance","Results:");
            DebugWindow.reportf(DebugWindow.PERFORMANCE, "enityop","    %1$fms", entityop);
            DebugWindow.reportf(DebugWindow.PERFORMANCE, "enityturn","  %1$fms", entityturn);
            DebugWindow.reportf(DebugWindow.PERFORMANCE, "tileupdate"," %1$fms", tileupdate);
            DebugWindow.reportf(DebugWindow.PERFORMANCE, "TOTAL","      %1$fms", total);
            DebugWindow.reportf(DebugWindow.PERFORMANCE, "","");
            DebugWindow.reportf(DebugWindow.PERFORMANCE, "LEVEL", " \'%1$s\':", currentLevel.getName());
            DebugWindow.reportf(DebugWindow.PERFORMANCE, "Level Dimensions"," %1$d x %2$d", currentLevel.getBaseTiles().length, currentLevel.getBaseTiles()[0].length);
            DebugWindow.reportf(DebugWindow.PERFORMANCE, "Overlay Tiles","    %1$d", currentLevel.getOverlayTiles().size());
            DebugWindow.reportf(DebugWindow.PERFORMANCE, "Total Entities","   %1$d", currentLevel.getEntities().size());
            DebugWindow.reportf(DebugWindow.PERFORMANCE, "Animated Tiles","   %1$d", currentLevel.getAnimatedTiles().size());
            DebugWindow.updateLayerInfo();
        }
    }

    public TextBox getTextBox() {
        return textBox;
    }

    public QuickMenu getQuickMenu() {
        return quickMenu;
    }

    public long getTurnCounter() {
        return turnCounter;
    }

    private interface EntityOperation{
        void run();
    }

    private transient ArrayList[] points; //The full list of points generated by the path-finding algorithm. Each position of the array contains a list of points a certain distance away form the player
    private transient ArrayList<PathPoint> pointLog = new ArrayList<>(); //A more raw list of points used during path-finding algorithm
    private transient HashMap<Long, Integer> entityPathMap = new HashMap<>(); //A map that matches entity UIDs to distances (# of steps required) from the player. Makes things super convenient for entities to check the path points

    private Layer pathTestLayer;

    public Layer getPathTestLayer() {
        return pathTestLayer;
    }

    private void calculatePathing(){
        entityPathMap = new HashMap<>();
        pathTestLayer.transpose(new Layer(currentLevel.getBackdrop().getCols(), currentLevel.getBackdrop().getRows(), "", 0, 0, 0));
        int max = 0;
        for (Entity entity : currentLevel.getEntities()){ //Figure out how large to spread out to.
            max = Math.max(max, entity.getPathingSize());
        }
        pointLog = new ArrayList<>();
        if (max > 0) {
            points = new ArrayList[max];
            ArrayList<PathPoint> initialSet = new ArrayList<>();
            initialSet.add(new PathPoint(getPlayer().getLocation(), 0));
            points[0] = initialSet; //A single point is needed to begin spreading out new ones
            for (int n = 1; n < max - 2; n++) {
                DebugWindow.reportf(DebugWindow.STAGE, "GameInstance.calculatePathing","n:%1$d points:%2$d", n, pointLog.size());
                points[n] = new ArrayList<>();
                for (Object obj : points[n - 1]) { //Fill in points at points[n], origin of checking for those new points at points[n-1]
                    if (obj instanceof PathPoint) {
                        PathPoint pos = (PathPoint) obj;
                        attemptPoint(pos.loc.add(new Coordinate(1, 0)), n);
                        attemptPoint(pos.loc.add(new Coordinate(0, 1)), n);
                        attemptPoint(pos.loc.add(new Coordinate(-1, 0)), n);
                        attemptPoint(pos.loc.add(new Coordinate(0, -1)), n);
                    }
                }
                //clearPointLog(n-2);
            }
        }
    }


    private Color[] testColors = {
            new Color(100, 50, 50, 50),
            new Color(99, 80, 50, 50),
            new Color(87, 97, 49, 50),
            new Color(59, 97, 49, 50),
            new Color(49, 97, 68, 50),
            new Color(49, 97, 97, 50),
            new Color(49, 68, 97, 50),
            new Color(60, 49, 97, 50),
            new Color(87, 49, 97, 50)
    };

    /**
     * Attempts to place a point at a location
     * @param loc The location to attempt
     * @param n The 'generation' of point to create
     */
    @SuppressWarnings("unchecked")
    private void attemptPoint(Coordinate loc, int n){
        if (points[n-1] == null) DebugWindow.reportf(DebugWindow.STAGE, "GameInstance.attemptPoint","List n=%1$d is null!", n);
        if (currentLevel.isLocationValid(loc) && !getTileAt(loc).hasTag(TagRegistry.NO_PATHING) && !pointLog.contains(new PathPoint(loc, -1))) {
            points[n].add(new PathPoint(loc, n));
            pointLog.add(new PathPoint(loc, n));
            //pathTestLayer.editLayer(loc.getX(), loc.getY(), new SpecialText(' ', Color.WHITE, testColors[n % testColors.length]));
        }
        for (Entity e : currentLevel.getEntitiesAt(loc)){
            if (!entityPathMap.containsKey(e.getUniqueID())) entityPathMap.put(e.getUniqueID(), n);
        }
    }

    private void clearPointLog(int nCutoff){
        for (int i = 0; i < pointLog.size(); i++){
            PathPoint pt = pointLog.get(i);
            if (pt.g <= nCutoff){
                pointLog.remove(i);
                i--;
            }
        }
    }

    void togglePathTestLayer(){
        pathTestLayer.setVisible(!pathTestLayer.getVisible());
        if (pathTestLayer.getVisible()){
            pathTestLayer.clearLayer();
            for (PathPoint pt : pointLog) {
                pathTestLayer.editLayer(pt.loc.getX(), pt.loc.getY(), new SpecialText(' ', Color.WHITE, testColors[pt.g % testColors.length]));
            }
        }
    }

    /**
     * Returns the 'generation' of points to check for a specific Entity
     * @param e The entity to match to
     * @return Where to look in the path points matrix. Returns -1 if the entity is not in the map, and therefore is not close enough to path to the player.
     */
    public int getEntityPlayerDistance(Entity e){
        return entityPathMap.getOrDefault(e.getUniqueID(), -1);
    }

    /**
     * Gets the array of points found at a certain distance
     * @param n The number of steps away from the player
     * @return The array of points an 'n' amount of steps from the player
     */
    public ArrayList getPathPoints(int n){
        if (n > 0 && n < points.length){
            return points[n];
        } else
            return null;
    }

    public class PathPoint{

        Coordinate loc;
        int g; //'g' is for generation

        private PathPoint(Coordinate loc, int g){
            this.loc = loc;
            this.g = g;
        }

        public Coordinate getLoc() {
            return loc;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof PathPoint) {
                PathPoint pathPoint = (PathPoint) obj;
                return pathPoint.loc.equals(loc);
            }
            return false;
        }
    }
}
