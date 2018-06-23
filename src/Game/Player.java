package Game;

import Data.*;
import Engine.Layer;
import Engine.SpecialText;
import Game.Debug.DebugWindow;
import Game.Entities.CombatEntity;
import Game.Entities.Entity;
import Game.Registries.TagRegistry;
import Game.Spells.Spell;
import Game.Tags.RangeTag;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Jared on 3/27/2018.
 */
public class Player extends CombatEntity implements MouseInputReceiver{

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

    private ArrayList<PlayerActionCollector> playerActionCollectors;

    private int magicPower = 0;
    private double weightCapacity = 20;

    private int noEnterWarpZoneTimer = 0;

    private Coordinate movementVector = new Coordinate(0, 0);
    private final Coordinate NORTH    = new Coordinate(0, -1);
    private final Coordinate EAST     = new Coordinate(1, 0);
    private final Coordinate SOUTH    = new Coordinate(0, 1);
    private final Coordinate WEST     = new Coordinate(-1, 0);
    private transient Thread movementThread;
    private final int MOVEMENT_INTERVAL = 125;

    private final SpecialText playerSprite = new SpecialText('@', new Color(223, 255, 214));

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

        playerActionCollectors = new ArrayList<>();
    }

    /**
     * Occurs upon the GameInstance initializing, gets the Player ready for game play.
     */
    void playerInit(){
        inv = new PlayerInventory(gi.getLayerManager(), this);
        hud = new HUD(gi.getLayerManager(), this);
        initSwooshLayer();
    }

    @Override
    protected void updateSprite() {
        getSprite().editLayer(0,0, new SpecialText(playerSprite.getCharacter(), colorateWithTags(playerSprite.getFgColor()), playerSprite.getBkgColor()));
        DebugWindow.reportf(DebugWindow.MISC, "Player.updateSprite","Original sprite %1$s ; Calculated: %2$s", getSprite().getSpecialText(0,0), playerSprite);
    }

    /**
     * Moves the camera position so that it is centered on the player
     */
    void updateCameraPos(){
        int cameraOffsetX = (gi.getLayerManager().getWindow().RESOLUTION_WIDTH / -2) - 1;
        int cameraOffsetY = (gi.getLayerManager().getWindow().RESOLUTION_HEIGHT / -2);
        int camNewX = getLocation().getX() + cameraOffsetX;
        int camNewY = getLocation().getY() + cameraOffsetY;
        if (gi != null)
            gi.getLayerManager().setCameraPos(Math.max(Math.min(camNewX, gi.getBackdrop().getCols() - gi.getLayerManager().getWindow().RESOLUTION_WIDTH), 0), Math.max(Math.min(camNewY, gi.getBackdrop().getRows() - gi.getLayerManager().getWindow().RESOLUTION_HEIGHT), -1));
        else
            gi.getLayerManager().setCameraPos(camNewX, camNewY);
        getSprite().setPos(getLocation());
    }


    public void updateHUD() {hud.updateHUD();}

    public void updateSynopsis() {
        if (mouseScreenPos != null)
            hud.updateSynopsis(mouseScreenPos.subtract(gi.getLayerManager().getCameraPos()));
    }

    /**
     * Checks for whether the player is standing in a warp zone (and hadn't just warped there) and moves to a new level if so.
     */
    private void checkForWarpZones(){
        ArrayList<WarpZone> warpZones = gi.getCurrentLevel().getWarpZones();
        for (WarpZone wz : warpZones){
            if (wz.isInsideZone(getLocation())){
                if (noEnterWarpZoneTimer == 0) {
                    noEnterWarpZoneTimer = 2;
                    terminatePathing = true;
                    FileIO io = new FileIO();
                    String path = io.getRootFilePath() + wz.getRoomFilePath();
                    Coordinate wzNewPos = new Coordinate(wz.getNewRoomStartX(), wz.getNewRoomStartY());
                    DebugWindow.reportf(DebugWindow.GAME, "Player.checkForWarpZones","Attempting level file path: %1$s \n* wz pos: %2$s\n", path, wzNewPos);
                    gi.enterLevel(path, wzNewPos.add(getLocation()).subtract(new Coordinate(wz.getXpos(), wz.getYpos())));
                }
            }
        }
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
    protected void move(int relativeX, int relativeY) {
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
        checkForWarpZones();
        updateCameraPos();
        if (inv.getOtherInv().getMode() == PlayerInventory.CONFIG_OTHER_EXCHANGE && inv.getOtherInv().isShowing()){
            inv.closeOtherInventory();
            inv.getPlayerInv().close();
        }
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
        if (getHealth() <= 0) gi.getGameMaster().exitGameToMainMenu();
    }

    @Override
    protected void doAttackEvent(CombatEntity ce) {
        /*if (getWeapon() != null && getWeapon().getItemData().getQty() == 1)
            removeItem(getWeapon());*/
        super.doAttackEvent(ce);
        hud.updateSynopsis(ce.getLocation());
        hud.updateHUD();
    }

    @Override
    public void onTurn() {
        super.onTurn();
        for (int i = 0; i < cooldowns.size(); i++) { //Decrement cooldowns
            int cd = cooldowns.get(i)-1;
            if (cd > 0)
                cooldowns.set(i, cooldowns.get(i)-1);
            else {
                cooldowns.remove(i);
                i--; //To account for the list shifting down
            }
        }
        if (noEnterWarpZoneTimer > 0) noEnterWarpZoneTimer--;
    }

    @Override
    protected void fireArrowProjectile(Projectile arrow) {
        doYellowFlash();
        doEnemyTurn();
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

    /**
     * Cleanup action done when GameInstance is closing
     */
    void cleanup(){
        hud.setPlayer(null);
        inv.setPlayer(null);
        gi = null;
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

    /**
     * Essentially, what happens when you push the 'Inventory' button (default: E)
     */
    private void toggleInventory(){
        if (inv.getPlayerInv().isShowing()) {
            inv.getPlayerInv().close();
            inv.closeOtherInventory();
        } else {
            inv.getPlayerInv().changeMode(PlayerInventory.CONFIG_PLAYER_USE);
            inv.getPlayerInv().show();
            inv.updateItemDescription(null);
        }
        updateHUD();
    }

    /*
    @Override
    public void keyPressed(KeyEvent e) {
        if (!downKeyCodes.contains(e.getExtendedKeyCode())){
            downKeyCodes.add(e.getExtendedKeyCode());
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SHIFT){
            spellMode = false;
            updateHUD();
        }
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) movementKeyUp(EAST);
        if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A)  movementKeyUp(WEST);
        if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S)  movementKeyUp(SOUTH);
        if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W)    movementKeyUp(NORTH);
        downKeyCodes.remove(new Integer(e.getExtendedKeyCode()));
        if (downKeyCodes.size() == 0) movementVector = new Coordinate(0, 0);
        DebugWindow.reportf(DebugWindow.STAGE, "Player movementVector","%1$s", movementVector);
    }
    */

    private void movementKeyDown(Coordinate vector){
        movementVector = movementVector.add(vector);
        movementVector = new Coordinate(Math.max(-1, Math.min(1, movementVector.getX())), Math.max(-1, Math.min(1, movementVector.getY()))); //Sanitates the movement vector
        if (movementThread == null || !movementThread.isAlive()) { //Probably should not rudely interrupt movement thread.
            movementThread = new Thread(() -> {
                while (!movementVector.equals(new Coordinate(0, 0))){ //Move while movement vector is not zero
                    long loopStartTime = System.nanoTime();
                    if (!isFrozen()) {
                        if (movementVector.getX() != 0) {
                            move(movementVector.getX(), 0);
                            gi.doEnemyTurn();
                        }
                        if (movementVector.getY() != 0) {
                            move(0, movementVector.getY());
                            gi.doEnemyTurn();
                        }
                    }
                    int runTime = (int)((System.nanoTime() - loopStartTime) / 1000000);
                    sleepMoveThread(Math.max(MOVEMENT_INTERVAL - runTime, 0));
                }
            });
            movementThread.start();
        }
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
        movementVector = movementVector.subtract(vector);
    }

    @Override
    public boolean onMouseMove(Coordinate levelPos, Coordinate screenPos) {
        mouseScreenPos = screenPos;
        if (gi.getCurrentLevel().isLocationValid(levelPos)) {
            DebugWindow.reportf(DebugWindow.CURSOR, "BASE TILE", "Tags: %1$s", gi.getCurrentLevel().getBaseTiles()[levelPos.getX()][levelPos.getY()].getTagList());
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
        if (!isFrozen() && actions != null && actions.size() > 0) {
            terminatePathing = true;
            if (actions.contains(InputMap.INVENTORY)) {
                toggleInventory();
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
            if (actions.contains(InputMap.ATTACK)){
                playerAttack(levelPos);
            }
            if (actions.contains(InputMap.PASS_TURN)){
                Thread passTurnThread = new Thread(() -> {
                    freeze();
                    gi.doEnemyTurn();
                });
                passTurnThread.start();
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
        return false;
    }

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
            if (actions.contains(InputMap.CAST_SPELL)){
                castSpell(levelPos);
            }
            if (gi.getGameMaster().getMouseInput().getDownInputs().size() == 0){
                movementVector = new Coordinate(0, 0);
            }
        }
        return false;
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
        quickMenu.addMenuItem("Load Game",    new Color(255, 230, 170), () -> gi.getGameMaster().openGameLoadMenu());
        quickMenu.addMenuItem("Controls",     new Color(173, 255, 228), () -> gi.getGameMaster().openKeybindMenu());
        quickMenu.addMenuItem("Quit to Menu", new Color(255, 171, 171), () -> gi.getGameMaster().exitGameToMainMenu());
        quickMenu.addMenuItem("Close",        () -> {});
        quickMenu.showMenu("Options", true);
    }

    private void readySpell(Coordinate levelPos){
        if (isSpellReady()) {
            equippedSpell.readySpell(levelPos, this, getGameInstance(), magicPower);
            for (PlayerActionCollector actionCollector : playerActionCollectors) actionCollector.onPlayerReadySpell(levelPos, equippedSpell);
        }
    }

    private void castSpell(Coordinate levelPos){
        Thread spellThread = new Thread(() -> {
            if (isSpellReady()) {
                freeze(); //Stop player actions while casting spell
                int cd = equippedSpell.castSpell(levelPos, this, getGameInstance(), magicPower);
                for (PlayerActionCollector actionCollector : playerActionCollectors) actionCollector.onPlayerCastSpell(levelPos, equippedSpell);
                if (cd > 0) {
                    cooldowns.add(cd);
                    gi.doEnemyTurn();
                } else {
                    unfreeze();
                }
            }
        });
        spellThread.start();
    }

    private void playerAttack(Coordinate levelPos){
        Thread attackThread = new Thread(() -> {
            freeze();
            for (PlayerActionCollector actionCollector : playerActionCollectors) actionCollector.onPlayerAttack(levelPos, getWeapon());
            doWeaponAttack(levelPos);
            gi.doEnemyTurn();
        });
        attackThread.start();
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
        else {
            terminatePathing = false;
            pathingThread = new Thread(() -> {
                Coordinate prevPos = null;
                while ((prevPos == null || !prevPos.equals(getLocation())) && !terminatePathing) {
                    prevPos = new Coordinate(getLocation().getX(), getLocation().getY());
                    long loopStartTime = System.nanoTime();

                    if (getLocation().stepDistance(levelPos) <= 1){
                        ArrayList<Entity> entities = gi.getCurrentLevel().getEntitiesAt(levelPos);
                        if (entities.size() == 1) {
                            entities.get(0).onInteract(this);
                            for (PlayerActionCollector actionCollector : playerActionCollectors) actionCollector.onPlayerInteract(entities.get(0).getLocation());
                            return; //Don't try to move into the thing you're trying to interact with.
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
                            return;
                        }
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

    public void doEnemyTurn(){
        gi.doEnemyTurn();
    }

    public void freeze() {gi.setPlayerTurn(false);}

    void unfreeze() {
        if (gi != null) {
            gi.setPlayerTurn(true);
        }
    }

    boolean isFrozen() {return !gi.isPlayerTurn(); }

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

    //Below is pathfinding stuff

    /*
    protected void pathToPosition(Coordinate target, int range){
        currentPoints = new ArrayList<>();
        futurePoints = new ArrayList<>();
        futurePoints.add(new SpreadPoint(target.getX(), target.getY(), 0));
        doPathing(1, range);
    }

    private transient ArrayList<SpreadPoint> currentPoints;
    private transient ArrayList<SpreadPoint> futurePoints;

    private void doPathing(int n, int detectRange){
        if (n > detectRange) return;
        moveFutureToPresentPoints();
        for (SpreadPoint point : currentPoints){
            attemptFuturePoint(point.x+1, point.y, n);
            attemptFuturePoint(point.x-1, point.y, n);
            attemptFuturePoint(point.x, point.y+1, n);
            attemptFuturePoint(point.x, point.y-1, n);
        }
        for (SpreadPoint pt : futurePoints){
            if (pt.x == getLocation().getX() && pt.y == getLocation().getY()){
                for (SpreadPoint cp : currentPoints){
                    if (getLocation().stepDistance(new Coordinate(cp.x, cp.y)) <= 1){
                        teleport(new Coordinate(cp.x, cp.y));
                        return;
                    }
                }
            }
        }
        doPathing(n+1, detectRange);
    }

    private void moveFutureToPresentPoints(){
        for (SpreadPoint point : futurePoints) if (!currentPoints.contains(point)) {
            currentPoints.add(point);
        }
        futurePoints.clear();
    }

    private void attemptFuturePoint(int col, int row, int generation){
        if (getGameInstance().isSpaceAvailable(new Coordinate(col, row), TagRegistry.NO_PATHING) || getLocation().equals(new Coordinate(col, row))) futurePoints.add(new SpreadPoint(col, row, generation));
    }

    private class SpreadPoint{
        int x;
        int y;
        int g; //Shorthand for 'generation'
        private SpreadPoint(int x, int y, int g){
            this.x = x;
            this.y = y;
            this.g = g;
        }
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof SpreadPoint){
                SpreadPoint other = (SpreadPoint)obj;
                return x == other.x && y == other.y;
            }
            return false;
        }

        @Override
        public String toString() {
            return String.format("PathPoint:[%1$d,%2$d,%3$d]", x, y, g);
        }
    }
    */
}
