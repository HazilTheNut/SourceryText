package Game;

import Data.*;
import Engine.Layer;
import Engine.SpecialText;
import Game.Debug.DebugWindow;
import Game.Entities.CombatEntity;
import Game.Entities.Entity;
import Game.Entities.GameCharacter;
import Game.LevelScripts.LevelScript;
import Game.LevelScripts.WaterFlow;
import Game.Registries.EntityRegistry;
import Game.Registries.LevelScriptRegistry;
import Game.Registries.TagRegistry;
import Game.Spells.Spell;
import Game.Tags.RangeTag;

import java.awt.*;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

/**
 * Created by Jared on 3/27/2018.
 */
public class Player extends GameCharacter implements GameInputReciever {

    /**
     * Player:
     *
     * The CombatEntity the player gets to control. It has special fields for the weight system, spell system, and movement system.
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private HUD hud;
    private PlayerInventory inv;

    private transient Thread pathingThread;
    private boolean terminatePathing;

    private Coordinate mouseScreenPos;

    private ArrayList<Spell> spells = new ArrayList<>();
    private Spell equippedSpell;
    private int numberSpellBeads = 1;
    private ArrayList<Integer> cooldowns = new ArrayList<>();
    private boolean castingSpell = false;

    private ArrayList<PlayerActionCollector> playerActionCollectors;

    private int magicPower;
    private double weightCapacity = 20;
    private PlayerWallet wallet;

    private transient Item itemToThrow = null;

    private int noEnterWarpZoneTimer = 0;

    private final Coordinate NORTH    = new Coordinate(0, -1);
    private final Coordinate EAST     = new Coordinate(1, 0);
    private final Coordinate SOUTH    = new Coordinate(0, 1);
    private final Coordinate WEST     = new Coordinate(-1, 0);
    private final Coordinate PASS     = new Coordinate(0, 0);
    private transient ArrayList<Coordinate> movementVectorList;
    private boolean terminateMovement;
    private final int MOVEMENT_INTERVAL = 125;

    private boolean onRaft = false;

    private final SpecialText playerSprite = new SpecialText('@', new Color(223, 255, 214));
    private final SpecialText raftSprite   = new SpecialText('@', new Color(223, 255, 214), new Color(71, 47, 30, 240));

    Player(GameInstance gameInstance){

        Layer playerLayer = new Layer(new SpecialText[1][1], "player", 0, 0, LayerImportances.ENTITY_SOLID);
        playerLayer.editLayer(0, 0, playerSprite);

        setSprite(playerLayer);

        gi = gameInstance;

        setLocation(new Coordinate(0, 0));

        setMaxHealth(20);
        setStrength(1);

        setName("Player");

        addTag(TagRegistry.FLAMMABLE, this);
        addTag(TagRegistry.LIVING, this);
        initNoWeapon();

        wallet = new PlayerWallet(new ItemStruct(10000, 1, "Wallet", 0), gi);
        wallet.addTag(TagRegistry.UNLIMITED_USAGE, this);
        wallet.addTag(TagRegistry.IMPORTANT, this);
        addItem(wallet);

        playerActionCollectors = new ArrayList<>();

        isAutonomous = false;
        factionAlignments = new ArrayList<>();
        factionAlignments.add("player");
    }

    /**
     * Occurs upon the GameInstance initializing, gets the Player ready for game play.
     */
    void playerInit(){
        inv = new PlayerInventory(gi.getLayerManager(), this);
        hud = new HUD(gi);
        initSwooshLayer();
        movementVectorList = new ArrayList<>();
        startMovementThread();
    }

    @Override
    public void updateSprite() {
        SpecialText icon = (onRaft) ? raftSprite : playerSprite;
        getSprite().editLayer(0,0, new SpecialText(icon.getCharacter(), colorateWithTags(icon.getFgColor()), icon.getBkgColor()));
        DebugWindow.reportf(DebugWindow.MISC, "Player.updateSprite","Original sprite %1$s ; Calculated: %2$s", getSprite().getSpecialText(0,0), playerSprite);
    }

    /**
     * Moves the camera position so that it is centered on the player
     */
    public void updateCameraPos(){
        int cameraOffsetX = (gi.getLayerManager().getWindow().RESOLUTION_WIDTH / -2) - 1;
        int cameraOffsetY = (gi.getLayerManager().getWindow().RESOLUTION_HEIGHT / -2);
        int camNewX = getLocation().getX() + cameraOffsetX;
        int camNewY = getLocation().getY() + cameraOffsetY;
        if (gi != null)
            gi.getLayerManager().setCameraPos(Math.max(Math.min(camNewX, gi.getBackdrop().getCols() - gi.getLayerManager().getWindow().RESOLUTION_WIDTH), 0), Math.max(Math.min(camNewY, gi.getBackdrop().getRows() - gi.getLayerManager().getWindow().RESOLUTION_HEIGHT), -1));
        else
            gi.getLayerManager().setCameraPos(camNewX, camNewY);
        getSprite().setPos(getLocation());
        updateSynopsis();
    }

    public void updateHUD() {hud.updateHUD();}

    public void updateSynopsis() {
        hud.updateSynopsis();
    }

    /**
     * Checks for whether the player is standing in a warp zone (and hadn't just warped there) and moves to a new level if so.
     *
     * @return Returns if the player is moving onto a warp zone and the player transitioned to a new level.
     */
    boolean checkForWarpZones(Coordinate nextPos){
        ArrayList<WarpZone> warpZones = gi.getCurrentLevel().getWarpZones();
        for (WarpZone wz : warpZones){
            if (wz.isInsideZone(nextPos)){
                if (noEnterWarpZoneTimer == 0) {
                    noEnterWarpZoneTimer = 2;
                    terminatePathing = true;
                    FileIO io = new FileIO();
                    String path = io.getRootFilePath() + wz.getRoomFilePath();
                    Coordinate wzNewPos = new Coordinate(wz.getNewRoomStartX(), wz.getNewRoomStartY());
                    DebugWindow.reportf(DebugWindow.GAME, "Player.checkForWarpZones","Attempting level file path: %1$s \n* wz pos: %2$s\n", path, wzNewPos);
                    gi.enterLevel(path, wzNewPos.add(nextPos).subtract(new Coordinate(wz.getXpos(), wz.getYpos())));
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void scanInventory() {
        super.scanInventory();
        if (inv.getPlayerInv().isShowing())
            inv.getPlayerInv().updateDisplay();
        updateHUD();
    }

    @Override
    public void updateInventory() {
        super.updateInventory();
        inv.updateDisplays();
        updateHUD();
    }

    @Override
    public void move(int relativeX, int relativeY) {
        while (gi.getLayerManager().isDrawingFrame())
            turnSleep(2); //Moving causes the camera to move, which can cause screen tearing and other "jumpiness" that is undesirable
        Coordinate nextPos = getLocation().copy().add(new Coordinate(relativeX, relativeY));
        if (checkForWarpZones(nextPos))
            return;
        if (doRaftStuff(relativeX, relativeY))
            super.move(relativeX, relativeY);
        for (PlayerActionCollector actionCollector : playerActionCollectors) actionCollector.onPlayerMove(getLocation());
        postMovementCheck();
    }

    @Override
    public void setPos(Coordinate pos) {
        super.setPos(pos);
        postMovementCheck();
    }

    /**
     * Performs checks after any kind of movement, aside from setPos()
     */
    private void postMovementCheck(){
        if (inv.getOtherInv().getMode() == PlayerInventory.CONFIG_OTHER_EXCHANGE && inv.getOtherInv().isShowing()){
            inv.closeOtherInventory();
            inv.getPlayerInv().close();
        }
    }

    /**
     * Does everything that has to do with rafts and flowing water
     *
     * @return if regular movement should be processed
     */
    private boolean doRaftStuff(int relativeX, int relativeY){
        Coordinate nextPos = getLocation().copy().add(new Coordinate(relativeX, relativeY));
        Tile nextTile = gi.getCurrentLevel().getTileAt(nextPos);
        //Check for getting onto a raft
        if (!onRaft){
            Entity raft = searchForRaft(nextPos);
            if (raft != null) {
                onRaft = true;
                updateSprite();
                setPos(nextPos);
                raft.selfDestruct();
                return false;
            }
        } else if (gi.isSpaceAvailable(nextPos, TagRegistry.TILE_WALL)){
            //Check for getting off of a raft
            if (!nextTile.hasTag(TagRegistry.DEEP_WATER)){
                gi.instantiateEntity(EntityRegistry.getEntityStruct(EntityRegistry.RAFT), getLocation().copy(), gi.getCurrentLevel());
                onRaft = false;
                updateSprite();
            } else {
                //Check for movement while on raft
                if (waterFlowAllowsMovement(getLocation(), relativeX, relativeY)) {
                    setPos(nextPos);
                    return false;
                }
            }
        }
        return true;
    }

    private Entity searchForRaft(Coordinate loc){
        ArrayList<Entity> entities = gi.getCurrentLevel().getEntitiesAt(loc);
        for (Entity e : entities){
            if (e.getName().equals("Raft"))
                return e;
        }
        return null;
    }

    private boolean waterFlowAllowsMovement(Coordinate loc, int relativeX, int relativeY){
        WaterFlow waterFlowScript = (WaterFlow)gi.getCurrentLevel().getLevelScript(LevelScriptRegistry.SCRIPT_WATERFLOW);
        if (waterFlowScript != null) {
            return waterFlowScript.getFlowDirection(loc).boxDistance(new Coordinate(relativeX, relativeY)) <= 1;
        }
        return true;
    }

    public boolean isOnRaft() {
        return onRaft;
    }

    @Override
    public void heal(int amount) {
        super.heal(amount);
        hud.updateHUD();
    }

    @Override
    public void receiveDamage(int amount) {
        super.receiveDamage(amount);
        hud.updateHUD();
        if (pathingThread != null && pathingThread.isAlive()) terminatePathing = true;
    }

    @Override
    public void selfDestruct() {
        super.selfDestruct();
        gi.getDeathMenu().show();
    }

    @Override
    protected void doAttackEvent(CombatEntity ce) {
        /*if (getWeapon() != null && getWeapon().getItemData().getQty() == 1)
            removeItem(getWeapon());*/
        super.doAttackEvent(ce);
        hud.updateHUD();
    }

    @Override
    public void onTurn() {
        super.onTurn();
        decrementCooldowns();
        if (noEnterWarpZoneTimer > 0) noEnterWarpZoneTimer--;
    }

    @Override
    public void setTarget(CombatEntity target) { } //Should do nothing for the player

    public void decrementCooldowns(){
        for (int i = 0; i < cooldowns.size(); i++) {
            int cd = cooldowns.get(i)-1; //The new cooldown amount to assign
            if (cd > 0)
                cooldowns.set(i, cooldowns.get(i)-1);
            else {
                cooldowns.remove(i);
                i--; //To account for the list shifting down
            }
        }
    }

    @Override
    protected void fireArrowProjectile(Projectile arrow) {
        doYellowFlash();
        doEnemyTurn();
        freeze(); //Because GameInstance automatically unfreezes the player after processing a turn, but firing the arrow has yet to happen
        turnSleep(100);
        if (shouldDoAction()) {
            RangeTag rangeTag = (RangeTag) getWeapon().getTag(TagRegistry.RANGE_START);
            if (rangeTag == null)
                arrow.launchProjectile(RangeTag.RANGE_DEFAULT);
            else
                arrow.launchProjectile(rangeTag.getRange());
            getWeapon().decrementQty();
        }
    }

    @Override
    public void addItem(Item item) {
        if (item.hasTag(TagRegistry.MONEY)) {
            addMoney(item.getItemData().getQty(), item.getItemData().getName());
            updateInventory();
            return;
        }
        super.addItem(item);
    }

    /**
     * Cleanup action done when GameInstance is closing
     */
    void cleanup(){
        hud.setPlayer(null);
        inv.setPlayer(null);
        gi = null;
    }

    @Override
    public CombatEntity getNearestEnemy() {
        return null;
    }

    @Override
    protected void pickNewWeapon() {

    }

    @Override
    public long getUniqueID() {
        return 0;
    }

    public PlayerInventory getInv() { return inv; }

    public Spell getEquippedSpell() {
        return equippedSpell;
    }

    public ArrayList<Spell> getSpells() {
        return spells;
    }

    public void setEquippedSpell(Spell equippedSpell) {
        this.equippedSpell = equippedSpell;
    }

    void assignMouseInput(GameMouseInput mi){
        mi.addInputReceiver(hud);
        mi.addInputReceiver(inv);
        mi.addInputReceiver(this);
    }

    public void addPlayerActionCollector(PlayerActionCollector actionCollector) {
        playerActionCollectors.add(actionCollector);
    }

    public void removePlayerActionCollector(PlayerActionCollector actionCollector){
        playerActionCollectors.remove(actionCollector);
    }

    private Coordinate compileMovementVectors(){
        try {
            Coordinate result = new Coordinate(0, 0);
            for (Coordinate vector : movementVectorList)
                if (vector != null) result = result.add(vector);
            return result;
        } catch (ConcurrentModificationException e){
            e.printStackTrace();
        }
        return new Coordinate(0,0);
    }

    private void movementKeyDown(Coordinate vector){
        movementVectorList.add(vector);
    }

    private void reportMovementVector(){
        DebugWindow.reportf(DebugWindow.STAGE, "Player.movementVector", "%1$s", compileMovementVectors());
    }

    private void startMovementThread(){
        Thread movementThread = new Thread(() -> {
            while (!terminateMovement) {
                while (movementVectorList.size() < 1)
                    sleepMoveThread(10); //Wait for input
                Coordinate totalVector = compileMovementVectors();
                long loopStartTime = System.nanoTime();
                if (isNotFrozen()) {
                    freeze();
                    if (totalVector.getX() != 0) {
                        move(totalVector.getX(), 0);
                        gi.doEnemyTurn();
                    }
                    if (totalVector.getY() != 0) {
                        move(0, totalVector.getY());
                        gi.doEnemyTurn();
                    }
                    if (totalVector.equals(new Coordinate(0, 0)))
                        gi.doEnemyTurn();
                    unfreeze();
                }
                int runTime = (int) ((System.nanoTime() - loopStartTime) / 1000000);
                int interval = (!totalVector.equals(new Coordinate(0, 0))) ? MOVEMENT_INTERVAL : MOVEMENT_INTERVAL * 1;
                sleepMoveThread(Math.max(interval - runTime, 0));
                reportMovementVector();
            }
        });
        movementThread.start();
    }

    /**
     * Does the try/catch stuff that usually looks messy
     *
     * @param time Amount of time to sleep, in milliseconds
     */
    private void sleepMoveThread(int time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
            DebugWindow.reportf(DebugWindow.STAGE, "ERROR", e.getMessage());
        }
    }

    private void movementKeyUp(Coordinate vector){
        movementVectorList.remove(vector);
    }

    @Override
    public boolean onMouseMove(Coordinate levelPos, Coordinate screenPos) {
        mouseScreenPos = screenPos;
        if (gi.getCurrentLevel().isLocationValid(levelPos)) {
            DebugWindow.reportf(DebugWindow.CURSOR, "BASE TILE", "Age: %1$d Tags: %2$s", gi.getCurrentLevel().getBaseTileAt(levelPos).getAge(), gi.getCurrentLevel().getBaseTileAt(levelPos).getTagList());
            Tile overlay = gi.getCurrentLevel().getOverlayTileAt(levelPos);
            if (overlay != null) {
                DebugWindow.reportf(DebugWindow.CURSOR, "OVERLAY TILE", "Tags: %1$s", overlay.getTagList());
            } else {
                DebugWindow.reportf(DebugWindow.CURSOR, "OVERLAY TILE", " - -");
            }
            ArrayList<Entity> entities = gi.getCurrentLevel().getEntitiesAt(levelPos);
            for (int i = 0; i < entities.size(); i++) {
                DebugWindow.reportf(DebugWindow.CURSOR, "ENTTIY " + (i+1), "Tags: %1$s", entities.get(i).getTagList());
            }
            DebugWindow.reportf(DebugWindow.CURSOR, "LEVEL POS", "%1$s", levelPos);
            DebugWindow.reportf(DebugWindow.CURSOR, "MOUES POS", "%1$s", mouseScreenPos);
            if (castingSpell) {
                equippedSpell.spellDrag(levelPos, this, gi, magicPower);
                for (PlayerActionCollector actionCollector : playerActionCollectors) actionCollector.onPlayerDragSpell(levelPos, equippedSpell);
            }
        }
        return false;
    }

    @Override
    public boolean onMouseClick(Coordinate levelPos, Coordinate screenPos, int mouseButton) {
        return false;
    }

    @Override
    public boolean onMouseWheel(Coordinate levelPos, Coordinate screenPos, double wheelMovement) {
        return false;
    }

    @Override
    public boolean onInputDown(Coordinate levelPos, Coordinate screenPos, ArrayList<Integer> actions) {
        if (actions != null && actions.size() > 0) {
            terminatePathing = true;
            if (actions.contains(InputMap.INVENTORY)) {
                updateHUD(); //The inventory close and open stuff is handled by PlayerInventory
            }
            if (actions.contains(InputMap.INSPECT)) {
                inspect(levelPos);
            }
            if (actions.contains(InputMap.OPEN_MENU)) {
                openMenu();
            }
            if (actions.contains(InputMap.CAST_SPELL)){
                readySpell(levelPos);
            }
            if (actions.contains(InputMap.CHANGE_SPELL)){
                spellMenu();
            }
            if (actions.contains(InputMap.ATTACK)){
                playerAttack(levelPos);
            }
            if (actions.contains(InputMap.PASS_TURN)){
                movementKeyDown(PASS);
            }
            if (actions.contains(InputMap.THROW_ITEM)){
                if (getWeapon().getItemData().getItemId() > 0)
                    enterThrowingMode(getWeapon());
            }
            if (actions.contains(InputMap.MOVE_INTERACT)){
                moveAndInteract(levelPos);
            }
            if (actions.contains(InputMap.MOVE_NORTH)){
                movementKeyDown(NORTH);
            }
            if (actions.contains(InputMap.MOVE_SOUTH)){
                movementKeyDown(SOUTH);
            }
            if (actions.contains(InputMap.MOVE_EAST)){
                movementKeyDown(EAST);
            }
            if (actions.contains(InputMap.MOVE_WEST)){
                movementKeyDown(WEST);
            }
        }
        return true;
    }

    private boolean midThrowingAnimation = false; //If the player releases the throw item button while the player's shadow is throwing an item, the item being thrown suddenly becomes null.

    @Override
    public boolean onInputUp(Coordinate levelPos, Coordinate screenPos, ArrayList<Integer> actions) {
        if (actions != null) {
            if (actions.contains(InputMap.MOVE_NORTH)) {
                movementKeyUp(NORTH);
            }
            if (actions.contains(InputMap.MOVE_SOUTH)) {
                movementKeyUp(SOUTH);
            }
            if (actions.contains(InputMap.MOVE_EAST)) {
                movementKeyUp(EAST);
            }
            if (actions.contains(InputMap.MOVE_WEST)) {
                movementKeyUp(WEST);
            }
            if (actions.contains(InputMap.PASS_TURN)) {
                movementKeyUp(PASS);
            }
            if (actions.contains(InputMap.CAST_SPELL)){
                castSpell(levelPos);
            }
            if (actions.contains(InputMap.THROW_ITEM)){
                exitThrowingMode();
            }
        }
        return true;
    }

    @Override
    public boolean onNumberKey(Coordinate levelPos, Coordinate screenPos, int number) {
        DebugWindow.reportf(DebugWindow.STAGE, "Player.onNumberKey", "Last key #: %1$d", number);
        int index = (number != 0) ? number - 1 : 9;
        if (index < spells.size())
            equippedSpell = spells.get(index);
        updateHUD();
        return true;
    }

    private void inspect(Coordinate levelPos){
        ArrayList<Entity> entities = gi.getCurrentLevel().getEntitiesAt(levelPos);
        if (entities.size() == 1) inv.openOtherInventory(entities.get(0));
        else if (entities.size() > 1){
            QuickMenu quickMenu = gi.getQuickMenu();
            quickMenu.clearMenu();
            for (int i = 0; i < entities.size(); i++) {
                int finalI = i;
                quickMenu.addMenuItem(entities.get(i).getName(), () -> inv.openOtherInventory(entities.get(finalI)));
            }
            quickMenu.showMenu("Inspect:", true);
        } else {
            inv.openOtherInventory(null);
        }
    }

    private void openMenu(){
        QuickMenu quickMenu = gi.getQuickMenu();
        quickMenu.clearMenu();
        quickMenu.addMenuItem("Save Game",    new Color(171, 241, 255), () -> gi.getGameMaster().openGameSaveMenu());
        quickMenu.addMenuItem("Load Game",    new Color(255, 230, 170), () -> gi.getGameMaster().openGameLoadMenu());
        quickMenu.addMenuItem("Controls",     new Color(173, 255, 228), () -> gi.getGameMaster().openKeybindMenu());
        quickMenu.addMenuItem("Quit to Menu", new Color(255, 171, 171), () -> gi.getGameMaster().exitGameToMainMenu());
        quickMenu.addMenuItem("Close",        () -> {});
        quickMenu.showMenu("Options", true);
    }

    private void spellMenu(){
        QuickMenu quickMenu = gi.getQuickMenu();
        quickMenu.clearMenu();
        for (Spell spell : spells){
            quickMenu.addMenuItem(spell.getName(), spell.getColor(), () -> {
                setEquippedSpell(spell);
                updateHUD();
            });
        }
        quickMenu.showMenu("Spells", true);
    }

    private void readySpell(Coordinate levelPos){
        if (isSpellReady()) {
            equippedSpell.readySpell(levelPos, this, getGameInstance(), magicPower);
            castingSpell = true;
            for (PlayerActionCollector actionCollector : playerActionCollectors) actionCollector.onPlayerReadySpell(levelPos, equippedSpell);
        }
    }

    private void castSpell(Coordinate levelPos){
        Thread spellThread = new Thread(() -> {
            if (isSpellReady() && isNotFrozen()) {
                freeze(); //Stop player actions while casting spell
                int cd = 0;
                if (shouldDoAction()) {
                    cd = equippedSpell.castSpell(levelPos, this, getGameInstance(), magicPower);
                    for (PlayerActionCollector actionCollector : playerActionCollectors)
                        actionCollector.onPlayerCastSpell(levelPos, equippedSpell);
                }
                if (cd > 0) {
                    cooldowns.add(cd);
                    gi.doEnemyTurn();
                } else {
                    unfreeze();
                }
                castingSpell = false;
            }
        });
        spellThread.start();
    }

    private void playerAttack(Coordinate levelPos){
        if (isNotFrozen()) {
            Thread attackThread = new Thread(() -> {
                freeze();
                if (itemToThrow == null)
                    doStandardAttack(levelPos);
                else {
                    midThrowingAnimation = true;
                    for (PlayerActionCollector actionCollector : playerActionCollectors)
                        actionCollector.onPlayerThrowItem(levelPos, itemToThrow);
                    throwItem(itemToThrow, levelPos);
                    midThrowingAnimation = false;
                    exitThrowingMode();
                }
                gi.doEnemyTurn();
            });
            attackThread.start();
        }
    }

    private void doStandardAttack(Coordinate levelPos){
        for (PlayerActionCollector actionCollector : playerActionCollectors)
            actionCollector.onPlayerAttack(levelPos, getWeapon());
        doWeaponAttack(levelPos);
    }

    void enterThrowingMode(Item toThrow){
        itemToThrow = toThrow;
        updateHUD();
    }

    void exitThrowingMode(){
        if (!midThrowingAnimation) {
            itemToThrow = null;
            updateHUD();
        }
    }

    private boolean isSpellReady(){
        return cooldowns.size() < numberSpellBeads && equippedSpell != null;
    }

    public int getNumberSpellBeads() {
        return numberSpellBeads;
    }

    public void incrementSpellBeads() {
        numberSpellBeads++;
    }

    public ArrayList<Integer> getCooldowns() {
        return cooldowns;
    }

    private void moveAndInteract(Coordinate levelPos){
        if (pathingThread != null && pathingThread.isAlive())
            terminatePathing = true;
        else if (isNotFrozen()){
            terminatePathing = false;
            pathingThread = new Thread(() -> {
                Coordinate prevPos = null;
                while ((prevPos == null || !prevPos.equals(getLocation())) && !terminatePathing) {
                    prevPos = new Coordinate(getLocation().getX(), getLocation().getY());
                    long loopStartTime = System.nanoTime();

                    if (getLocation().stepDistance(levelPos) <= 1){
                        if (interactAt(levelPos))
                            return;
                    }

                    pathToPosition(levelPos, 150); //Limit of 150 steps away to prevent the game hanging for too long.
                    gi.doEnemyTurn();

                    float runTime = (System.nanoTime() - loopStartTime) / 1000000f;
                    DebugWindow.reportf(DebugWindow.STAGE, "Player.moveAndInteract", "Loop time: %1$fms", runTime);
                    try {
                        Thread.sleep(Math.max((int)(MOVEMENT_INTERVAL * 0.9) - (int)runTime, 0));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            });
            pathingThread.start();
        }
    }

    private boolean interactAt(Coordinate loc){
        for (LevelScript ls : gi.getCurrentLevel().getLevelScripts()){
            if (ls.onPlayerInteract(loc)) return true;
        }
        ArrayList<Entity> entities = gi.getCurrentLevel().getEntitiesAt(loc);
        if (entities.size() == 1) {
            entities.get(0).onInteract(this);
            for (PlayerActionCollector actionCollector : playerActionCollectors) actionCollector.onPlayerInteract(entities.get(0).getLocation());
            return true; //Don't try to move into the thing you're trying to interact with.
        }
        else if (entities.size() > 1){
            QuickMenu quickMenu = gi.getQuickMenu(); //Create menu of options to interact with
            quickMenu.clearMenu();
            for (int i = 0; i < entities.size(); i++) {
                int finalI = i;
                quickMenu.addMenuItem(entities.get(i).getName(), () -> {
                    entities.get(finalI).onInteract(this);
                    for (PlayerActionCollector actionCollector : playerActionCollectors) actionCollector.onPlayerInteract(entities.get(finalI).getLocation());
                });
            }
            quickMenu.showMenu("Interact:", true);
            return true;
        }
        return false;
    }

    @Override
    public void onInteract(Player player) {
        //Player passes turn if it interacts with itself. This allows game play using just the mouse.
        freeze();
        gi.doEnemyTurn();
        //Mouse-only game play assumes that the mouse being used has the forward and backward mouse buttons (called "mouse4" and "mouse 5")
        /*
        Possible Controls:
            Movement & Interaction : Right-Click
            Attack :                 Left-Click
            Cast Spell :             Middle-Click
            Change Spell :           (Click on Spell on HUD for drop-down menu)
            Pass Turn :              (Interact with self)
            Throw Item :             Mouse Button 4
            Inventory :              Mouse Button 5
         */
    }

    public void doEnemyTurn(){
        gi.doEnemyTurn();
    }

    public void freeze() {gi.setPlayerTurn(false);}

    public void unfreeze() {
        if (gi != null) {
            gi.setPlayerTurn(true);
        }
    }

    boolean isNotFrozen() { return gi.isPlayerTurn(); }

    public int getMagicPower() {
        return magicPower;
    }

    public void setMagicPower(int magicPower) {
        this.magicPower = magicPower;
    }

    public double getWeightCapacity() {
        return weightCapacity;
    }

    public void setWeightCapacity(double weightCapacity) {
        this.weightCapacity = weightCapacity;
    }

    public int getMoney(String currency){
        return wallet.getMoneyAmount(currency);
    }

    public void addMoney(int amount, String currency){
        wallet.addMoney(amount, currency);
    }

    public boolean dropItemsUntilUnderCapacity(){
        int dropIndex = 0; //The index of item list to drop. Although the list shifts down every time the items are removed, if it encounters an un-droppable item, it has to move to the next item.
        boolean droppedAnything = false;
        while(getInv().calculateTotalWeight() > weightCapacity && dropIndex < getItems().size()){
            if (dropItem(getItems().get(dropIndex)) == null)
                dropIndex++;
            else
                droppedAnything = true;
        }
        return droppedAnything;
    }

    public Item getItemToThrow() {
        return itemToThrow;
    }
}
