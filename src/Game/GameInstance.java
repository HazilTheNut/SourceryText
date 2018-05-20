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
import java.util.HashMap;
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
    private TextBox textBox;
    private QuickMenu quickMenu;

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

        textBox = new TextBox(lm, player);

        quickMenu = new QuickMenu(lm, player);

        /*pathTestLayer = new Layer(currentLevel.getBackdrop().getCols(), currentLevel.getBackdrop().getRows(), "pathtest", 0, 0, LayerImportances.VFX);
        lm.addLayer(pathTestLayer);*/
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
                    instantiateEntity(entityMatrix[col][row], new Coordinate(col, row), newLevel);
                }
            }
        }

        DebugWindow.reportf(DebugWindow.STAGE, "[GameInstance.loadLevel] Initialize tiles...");
        newLevel.initialize(ldata);

        DebugWindow.reportf(DebugWindow.STAGE, "[GameInstance.loadLevel] Assign warp zones...");
        newLevel.setWarpZones(ldata.getWarpZones());

        levels.add(newLevel);

        DebugWindow.reportf(DebugWindow.STAGE, "[GameInstance.loadLevel] Loading of level \'%1$s\' complete!", newLevel.getName());

        return newLevel;
    }

    /**
     * Instantiates an entity into a level, running initialize() and retrieving data from EntityRegistry.
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
        mi.addInputReceiver(textBox);
        mi.addInputReceiver(quickMenu);
        player.assignMouseInput(mi);
    }

    public void setPlayerTurn(boolean playerTurn) {
        isPlayerTurn = playerTurn;
    }

    public void addAnimatedTile(AnimatedTile animatedTile) { currentLevel.addAnimatedTile(animatedTile); }

    void doEnemyTurn(){
        //Thread enemyTurnThread = new Thread(() -> {
            long[] runTimes = new long[4];
            runTimes[0] = System.nanoTime();
            for (EntityOperation op : entityOperations) {
                op.run();
            }
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
            isPlayerTurn = true;
        //});
        //enemyTurnThread.start();
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
//            ArrayList<Layer> layerStack = lm.getLayerStack();
//            DebugWindow.reportf(DebugWindow.PERFORMANCE, "\nLayers: %1$d\n-------------------\n POS : PRI | NAME", layerStack.size());
//            for (int i = 0; i < layerStack.size(); i++) {
//                if (layerStack.get(i).getVisible())
//                    DebugWindow.reportf(DebugWindow.PERFORMANCE, " %1$3d : %2$3d | %3$s", i, layerStack.get(i).getImportance(), layerStack.get(i).getName());
//                else
//                    DebugWindow.reportf(DebugWindow.PERFORMANCE, " %1$3d : %2$3d | (-) %3$s", i, layerStack.get(i).getImportance(), layerStack.get(i).getName());
//            }
        }
    }

    public TextBox getTextBox() {
        return textBox;
    }

    public QuickMenu getQuickMenu() {
        return quickMenu;
    }

    private interface EntityOperation{
        void run();
    }

    private ArrayList[] points; //The full list of points generated by the path-finding algorithm. Each position of the array contains a list of points a certain distance away form the player
    private ArrayList<PathPoint> pointLog = new ArrayList<>(); //A more raw list of points used during path-finding algorithm
    private HashMap<Long, Integer> entityPathMap = new HashMap<>(); //A map that matches entity UIDs to distances (# of steps required) from the player. Makes things super convenient for entities to check the path points

    //private Layer pathTestLayer;

    private void calculatePathing(){
        entityPathMap.clear();
        //pathTestLayer.transpose(new Layer(currentLevel.getBackdrop().getCols(), currentLevel.getBackdrop().getRows(), "", 0, 0, 0));
        int max = 0;
        for (Entity entity : currentLevel.getEntities()){ //Figure out how large to spread out to.
            max = Math.max(max, entity.getPathingSize());
        }
        pointLog.clear();
        if (max > 0) {
            points = new ArrayList[max];
            ArrayList<PathPoint> initialSet = new ArrayList<>();
            initialSet.add(new PathPoint(player.getLocation(), 0));
            points[0] = initialSet; //A single point is needed to begin spreading out new ones
            for (int n = 1; n < max - 2; n++) {
                DebugWindow.reportf(DebugWindow.STAGE, "[GameInstance.calculatePathing] n:%1$d points:%2$d", n, pointLog.size());
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

   /*
    Color[] testColors = {
            new Color(100, 50, 50, 50),
            new Color(99, 80, 50, 50),
            new Color(87, 97, 49, 50),
            new Color(59, 97, 49, 50),
            new Color(49, 97, 68, 50),
            new Color(49, 97, 97, 50),
            new Color(49, 68, 97, 50),
            new Color(60, 49, 97, 50),
            new Color(87, 49, 97, 50)
    };*/

    /**
     * Attempts to place a point at a location
     * @param loc The location to attempt
     * @param n The 'generation' of point to create
     */
    @SuppressWarnings("unchecked")
    private void attemptPoint(Coordinate loc, int n){
        if (points[n-1] == null) DebugWindow.reportf(DebugWindow.STAGE, "[GameInstance.attemptPoint] List n=%1$d is null!", n);
        if (isSpaceAvailable(loc, TagRegistry.NO_PATHING) && !pointLog.contains(new PathPoint(loc, -1))) {
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

    /**
     * Returns the 'generation' of points to check for a specific Entity
     * @param e The entity to match to
     * @return Where to look in the path points matrix. Returns -1 if the entity is not in the map, and therefore is not close enough to path to the player.
     */
    public int getEntityPlayerDistance(Entity e){
        if (entityPathMap.containsKey(e.getUniqueID()))
            return entityPathMap.get(e.getUniqueID());
        else
            return -1;
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
