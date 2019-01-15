package Game;

import Data.*;
import Engine.FrameUpdateListener;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.AnimatedTiles.AnimatedTile;
import Game.Debug.DebugWindow;
import Game.Entities.Entity;
import Game.Entities.LootPile;
import Game.LevelScripts.LevelScript;
import Game.Registries.EntityRegistry;
import Game.Registries.TagRegistry;

import java.awt.*;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Jared on 3/27/2018.
 */
public class GameInstance implements Serializable, FrameUpdateListener {

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
    private transient DialogueOptionsMenu dialogueOptions;
    private transient GameDeathMenu deathMenu;
    private FactionManager factionManager;

    private long currentUID = 1;
    private long turnCounter = 0;

    private ArrayList<String> gameEvents;

    private Layer loadingScreenLayer;

    public GameInstance(){
        levels = new ArrayList<>();
        entityOperations = new ArrayList<>();
        gameEvents = new ArrayList<>();
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
     * Sets up the GameInstance upon startup of the game (whether by "New Game" or by loading a save)
     */
    void initialize(){
        entityOperations = new ArrayList<>();

        if (factionManager == null) {
            factionManager = new FactionManager();
            factionManager.initialize();
        }

        if (player == null) {
            player = new Player(this);
        }
        player.playerInit();

        startAnimations();

        textBox = new TextBox(lm, getPlayer());
        quickMenu = new QuickMenu(lm, getPlayer());
        dialogueOptions = new DialogueOptionsMenu(lm, getPlayer());
        deathMenu = new GameDeathMenu(this);

        pathTestLayer = new Layer(0, 0, "pathtest", 0, 0, LayerImportances.VFX);
        pathTestLayer.setVisible(false);
        lm.addLayer(pathTestLayer);

        loadingScreenLayer = new Layer(lm.getWindow().RESOLUTION_WIDTH, lm.getWindow().RESOLUTION_HEIGHT, "loading_screen", 0, 0, LayerImportances.LOADING_SCREEN);
        loadingScreenLayer.fixedScreenPos = true;
        drawLoadingScreen();
        lm.addLayer(loadingScreenLayer);
    }

    private void drawLoadingScreen(){
        loadingScreenLayer.fillLayer(new SpecialText(' ', Color.BLACK, Color.BLACK));
        String phrase = "Loading...";
        loadingScreenLayer.inscribeString(phrase, (loadingScreenLayer.getCols() - phrase.length()) / 2, loadingScreenLayer.getRows() / 2);
    }

    void stopAnimations(){
        lm.removeFrameUpdateListener(this);
    }

    /**
     * Starts the timer that updates the AnimatedTiles.
     */
    void startAnimations(){
        lm.addFrameUpdateListener(this);
    }

    /**
     * Closes the GameInstance, preparing everything to be garbage-collected.
     */
    void dispose(){
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
        deathMenu.close();
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
            loadingScreenLayer.setVisible(true);
        }
        String zoneName = getFilePathParentFolder(levelFilePath);
        if (currentZoneName == null || !currentZoneName.equals(zoneName)){ //If moved to a new zone
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
        getPlayer().setPos(loc);
        level.addEntity(getPlayer());
        level.onEnter(lm, this);
        getPlayer().updateCameraPos();
        getPlayer().unfreeze();
        Layer pathTestTranspose = new Layer(level.getWidth(), level.getHeight(), "", 0, 0, 0);
        pathTestLayer.transpose(pathTestTranspose);
        loadingScreenLayer.setVisible(false);
    }

    /**
     * Unpacks a level from storage, and automatically sets the current room to the newly unpacked one automatically.
     *
     * @param levelFilePath The non-relative file path to the level's file
     * @return returns the loaded level
     */
    private Level loadLevel(String levelFilePath){
        DebugWindow.reportf(DebugWindow.STAGE, "GameInstance.loadLevel", "Begin level load to memory from %1$s", levelFilePath);
        Level newLevel = new Level(levelFilePath);

        currentLevel = newLevel;

        FileIO io = new FileIO();

        DebugWindow.reportf(DebugWindow.STAGE, "GameInstance.loadLevel", "I/O Deserialization...");
        File levelFile = new File(levelFilePath);
        LevelData ldata = io.openLevel(levelFile);

        DebugWindow.reportf(DebugWindow.STAGE, "GameInstance.loadLevel", "Initialize level...");
        newLevel.initialize(ldata);
        for (LevelScript ls : newLevel.getLevelScripts()) ls.initialize(this, newLevel);

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

        newLevel.onLevelLoad();

        DebugWindow.reportf(DebugWindow.STAGE, "GameInstance.loadLevel", "Loading of level \'%1$s\' complete!", newLevel.getName());

        return newLevel;
    }

    /**
     * Instantiates an entity into a level, running initialize() and retrieving data from EntityRegistry.
     * Note: Unsafe, ConcurrentModificationExceptions are prone to occur.
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

    /**
     * Either adds an item to the LootPile found at the specified location, or creates a new one if it does not exist.
     *
     * @param toDrop The Item to drop onto the ground
     * @param loc The location of where the Item should drop
     * @return The LootPile where the Item was added to. Returns null if the Item cannot be dropped.
     */
    public LootPile dropItem(Item toDrop, Coordinate loc){
        if (toDrop.hasTag(TagRegistry.IMPORTANT) || currentLevel.getTileAt(loc).hasTag(TagRegistry.BOTTOMLESS)) return null;
        ArrayList<Entity> entities = getCurrentLevel().getEntitiesAt(loc);
        for (Entity e : entities){ //Search for a loot pile that already exists
            if (e instanceof LootPile) {
                LootPile lootPile = (LootPile) e;
                lootPile.addItem(toDrop);
                return lootPile;
            }
        }
        //Create a new loot pile, since one obviously doesn't exist
        DebugWindow.reportf(DebugWindow.GAME, "SubInventory.dropItem","Creating new loot pile");
        EntityStruct lootPileStruct = new EntityStruct(EntityRegistry.LOOT_PILE, "Loot", null);
        LootPile pile = (LootPile)instantiateEntity(lootPileStruct, loc, getCurrentLevel());
        pile.addItem(toDrop);
        return pile;
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

    public FactionManager getFactionManager() {
        return factionManager;
    }

    boolean isPlayerTurn() { return isPlayerTurn; }

    public Layer getBackdrop() {
        return currentLevel.getBackdrop();
    }

    public void removeEntity(Entity e){
        entityOperations.add(() -> currentLevel.removeEntity(e));
    }

    public void unloadLevel(Level level){
        levels.remove(level);
    }

    public void addEntity(EntityStruct entityStruct, Coordinate loc){
        entityOperations.add(() -> instantiateEntity(entityStruct, loc, currentLevel));
    }

    public void establishMouseInput(){
        mi.addInputReceiver(textBox);
        mi.addInputReceiver(quickMenu);
        mi.addInputReceiver(dialogueOptions);
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
        long[] runTimes = new long[6];
        runTimes[0] = System.nanoTime();
        currentLevel.onTurnStart();
        runTimes[1] = System.nanoTime();
        ArrayList<Entity> entities = currentLevel.getEntities();
        for (int i = 0; i < entities.size();) {
            Entity e = entities.get(i);
            long startTime = System.nanoTime();
            e.onTurn();
            float turntime = (System.nanoTime() - startTime) / 1000000f;
            DebugWindow.reportf(DebugWindow.ENTITY, String.format("Entity#%1$05d.onTurn", e.getUniqueID()), "Name: \'%1$-20s\' Pos: %2$-9s Time: %3$.3fms", e.getName(), e.getLocation(), turntime);
            i++;
        }
        runTimes[2] = System.nanoTime();
        for (EntityOperation op : entityOperations) {
            op.run();
        }
        entityOperations.clear();
        runTimes[3] = System.nanoTime();
        for (Tile tile : currentLevel.getAllTiles()){
            tile.onTurn(GameInstance.this);
        }
        runTimes[4] = System.nanoTime();
        currentLevel.onTurnEnd();
        runTimes[5] = System.nanoTime();
        reportUpdatePerformance(runTimes);
        getPlayer().updateHUD();
        turnCounter++;
        DebugWindow.reportf(DebugWindow.STAGE, "GameInstace:turnCounter", "%1$d", turnCounter);
        DebugWindow.reportf(DebugWindow.GAME,  "GameInstace", "TURN %1$d", turnCounter);
        getPlayer().updateSynopsis();
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
            double lsstart    = (double)(times[1] - times[0]) / 1000000;
            double entityturn = (double)(times[2] - times[1]) / 1000000;
            double entityop   = (double)(times[3] - times[2]) / 1000000;
            double tileupdate = (double)(times[4] - times[3]) / 1000000;
            double lsend      = (double)(times[5] - times[4]) / 1000000;
            double total      = (double)(times[5] - times[0]) / 1000000;
            DebugWindow.reportf(DebugWindow.PERFORMANCE, "GameInstance.reportUpdatePerformance","Results:");
            DebugWindow.reportf(DebugWindow.PERFORMANCE, "ls_start","   %1$fms", lsstart);
            DebugWindow.reportf(DebugWindow.PERFORMANCE, "enityturn","  %1$fms", entityturn);
            DebugWindow.reportf(DebugWindow.PERFORMANCE, "enityop","    %1$fms", entityop);
            DebugWindow.reportf(DebugWindow.PERFORMANCE, "tileupdate"," %1$fms", tileupdate);
            DebugWindow.reportf(DebugWindow.PERFORMANCE, "ls_end","     %1$fms", lsend);
            DebugWindow.reportf(DebugWindow.PERFORMANCE, "TOTAL","      %1$fms", total);
            DebugWindow.reportf(DebugWindow.PERFORMANCE, "","");
            DebugWindow.reportf(DebugWindow.PERFORMANCE, "LEVEL", " \'%1$s\':", currentLevel.getName());
            DebugWindow.reportf(DebugWindow.PERFORMANCE, "Level Dimensions"," %1$d x %2$d", currentLevel.getBaseTiles().length, currentLevel.getBaseTiles()[0].length);
            DebugWindow.reportf(DebugWindow.PERFORMANCE, "Base Tiles","       %1$d", currentLevel.tileMatrixToList(currentLevel.getBaseTiles()).size());
            DebugWindow.reportf(DebugWindow.PERFORMANCE, "Overlay Tiles","    %1$d", currentLevel.tileMatrixToList(currentLevel.getOverlayTiles()).size());
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

    public ArrayList<String> getGameEvents() {
        return gameEvents;
    }

    public boolean eventHappened(String event){
        return gameEvents.contains(event);
    }

    public void recordEvent(String event){
        if (!gameEvents.contains(event))
            gameEvents.add(event);
    }

    public void levelScriptTrigger(String phrase){
        for (LevelScript script : currentLevel.getLevelScripts())
            script.onTrigger(phrase);
    }

    @Override
    public void onFrameDrawStart() {
        if (player != null && currentLevel.getBackdrop() != null){
            player.updateCameraPos();
        }
        if (currentLevel != null) {
            currentLevel.onAnimatedTileUpdate();
        }
    }

    @Override
    public void onFrameDrawEnd() {
        DebugWindow.reportf(DebugWindow.PERFORMANCE, "DrawTime"," %1$.3f", lm.getPreviousCompileTime() / 1000000f);
    }

    public GameDeathMenu getDeathMenu() {
        return deathMenu;
    }

    public DialogueOptionsMenu getDialogueOptions() {
        return dialogueOptions;
    }

    private interface EntityOperation{
        void run();
    }

    private Layer pathTestLayer;

    public Layer getPathTestLayer() {
        return pathTestLayer;
    }

    void onProjectileFly(Projectile projectile){
        for (ProjectileListener pl : currentLevel.getProjectileListeners())
            pl.onProjectileFly(projectile);
    }
}
