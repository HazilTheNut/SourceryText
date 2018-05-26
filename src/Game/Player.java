package Game;

import Data.*;
import Engine.Layer;
import Engine.SpecialText;
import Game.Debug.DebugWindow;
import Game.Entities.CombatEntity;
import Game.Entities.Entity;
import Game.Registries.TagRegistry;
import Game.Spells.Spell;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * Created by Jared on 3/27/2018.
 */
public class Player extends CombatEntity implements MouseInputReceiver, KeyListener{

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private HUD hud;
    private PlayerInventory inv;

    private transient Thread pathingThread;
    private boolean terminatePathing;

    private Coordinate mouseLevelPos;

    private boolean spellMode = false;

    private ArrayList<Spell> spells = new ArrayList<>();
    private Spell equippedSpell;
    private int numberSpellBeads = 1;
    private ArrayList<Integer> cooldowns = new ArrayList<>();

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
    
    private ArrayList<Integer> downKeyCodes = new ArrayList<>(); //KeyCodes of keys currently pressed down on the keyboard

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
        initNoWeapon();
    }

    void playerInit(){
        inv = new PlayerInventory(gi.getLayerManager(), this);
        hud = new HUD(gi.getLayerManager(), this);
        gi.getLayerManager().getWindow().addKeyListener(this);
        initSwooshLayer();
    }

    @Override
    protected void updateSprite() {
        getSprite().editLayer(0,0, new SpecialText(playerSprite.getCharacter(), colorateWithTags(playerSprite.getFgColor()), playerSprite.getBkgColor()));
        DebugWindow.reportf(DebugWindow.MISC, "Player.updateSprite","Original sprite %1$s ; Calculated: %2$s", getSprite().getSpecialText(0,0), playerSprite);
    }

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
    }

    public void updateHUD() {hud.updateHUD();}

    public void updateSynopsis() {hud.updateSynopsis(mouseLevelPos);}

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
        if (inv.getPlayerInv().isShowing())
            inv.getPlayerInv().updateDisplay();
        updateHUD();
    }

    @Override
    protected void move(int relativeX, int relativeY) {
        super.move(relativeX, relativeY);
        postMovementCheck();
    }

    @Override
    public void setPos(Coordinate pos) {
        super.setPos(pos);
        postMovementCheck();
    }

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
    }

    @Override
    protected void doAttackEvent(CombatEntity ce) {
        if (getWeapon() != null && getWeapon().getItemData().getQty() == 1)
            removeItem(getWeapon());
        super.doAttackEvent(ce);
        hud.updateSynopsis(ce.getLocation());
        hud.updateHUD();
    }

    @Override
    public void onTurn() {
        super.onTurn();
        for (int i = 0; i < cooldowns.size(); i++) {
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

    public void cleanup(){
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

    @Override
    public void keyTyped(KeyEvent e) {

    }

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

    @Override
    public void keyPressed(KeyEvent e) {
        if (!downKeyCodes.contains(e.getExtendedKeyCode())){
            onKeyDown(e.getExtendedKeyCode());
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

    //Holding down a key on the keyboard fires keyPressed() a gajillion times. This method is ONLY ran when a key is pressed down.
    private void onKeyDown(int keyCode){
        if (!isFrozen()) {
            if (keyCode == KeyEvent.VK_E){
                toggleInventory();
            } else if (keyCode == KeyEvent.VK_SHIFT){
                spellMode = true;
                updateHUD();
            } else if (keyCode == KeyEvent.VK_Q){
                ArrayList<Entity> entities = gi.getCurrentLevel().getEntitiesAt(mouseLevelPos);
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
            } else if (keyCode == KeyEvent.VK_ESCAPE) {
                QuickMenu quickMenu = gi.getQuickMenu();
                quickMenu.clearMenu();
                quickMenu.addMenuItem("Load Game",    new Color(255, 230, 170), () -> {
                    FileIO io = new FileIO();
                    gi.getGameMaster().loadGame(io.chooseSavedGame());
                });
                quickMenu.addMenuItem("Options",      new Color(173, 255, 228), () -> {});
                quickMenu.addMenuItem("Quit to Menu", new Color(255, 171, 171), () -> {
                    gi.getGameMaster().exitGame();
                });
                quickMenu.addMenuItem("Close",        () -> {});
                quickMenu.showMenu("Options", true);
            } else {
                if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) movementKeyDown(EAST);
                if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A)  movementKeyDown(WEST);
                if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S)  movementKeyDown(SOUTH);
                if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W)    movementKeyDown(NORTH);
            }
            terminatePathing = true;
        }
        DebugWindow.reportf(DebugWindow.STAGE, "Player movementVector","%1$s", movementVector);
    }

    private void movementKeyDown(Coordinate vector){
        movementVector = movementVector.add(vector);
        movementVector = new Coordinate(Math.max(-1, Math.min(1, movementVector.getX())), Math.max(-1, Math.min(1, movementVector.getY()))); //Sanitates the movement vector
        if (movementThread == null || !movementThread.isAlive()) {
            movementThread = new Thread(() -> {
                while (!movementVector.equals(new Coordinate(0, 0))){
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

    public boolean isInSpellMode() { return spellMode; }

    @Override
    public boolean onMouseMove(Coordinate levelPos, Coordinate screenPos) {
        mouseLevelPos = levelPos;
        return false;
    }

    @Override
    public boolean onMouseClick(Coordinate levelPos, Coordinate screenPos, int mouseButton) {
        if (mouseButton == MouseEvent.BUTTON1){
            DebugWindow.reportf(DebugWindow.STAGE, "Player.onMouseClick","Left click event! l:%1$s s:%2$s", levelPos, screenPos);
            doLeftClick(levelPos);
        } else if (mouseButton == MouseEvent.BUTTON3){
            DebugWindow.reportf(DebugWindow.STAGE, "Player.onMouseClick","Right click event! l:%1$s s:%2$s", levelPos, screenPos);
            doRightClick(levelPos);
        }
        return false;
    }

    @Override
    public boolean onMouseWheel(Coordinate levelPos, Coordinate screenPos, double wheelMovement) {
        return false;
    }

    private boolean isSpellReady(){
        return cooldowns.size() < numberSpellBeads;
    }

    public int getNumberSpellBeads() {
        return numberSpellBeads;
    }

    public ArrayList<Integer> getCooldowns() {
        return cooldowns;
    }

    private void doLeftClick(Coordinate levelPos){
        if (!isFrozen()) {
            if (!spellMode) {
                Thread attackThread = new Thread(() -> {
                    freeze();
                    doWeaponAttack(levelPos);
                    gi.doEnemyTurn();
                });
                attackThread.start();
            } else {
                DebugWindow.reportf(DebugWindow.GAME, "Player.onMouseClick","Spell casted! %1$s", levelPos);
                Thread spellThread = new Thread(() -> {
                    if (isSpellReady()) {
                        freeze();
                        cooldowns.add(equippedSpell.castSpell(levelPos, this, getGameInstance(), magicPower));
                        gi.doEnemyTurn();
                    }
                });
                spellThread.start();
            }
        }
    }

    private void doRightClick(Coordinate levelPos){
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
                        if (entities.size() == 1) entities.get(0).onInteract(this);
                        else if (entities.size() > 1){
                            QuickMenu quickMenu = gi.getQuickMenu();
                            quickMenu.clearMenu();
                            for (int i = 0; i < entities.size(); i++) {
                                int finalI = i;
                                quickMenu.addMenuItem(entities.get(i).getName(), () -> entities.get(finalI).onInteract(this));
                            }
                            quickMenu.showMenu("Interact:", true);
                        }
                        return;
                    }

                    pathToPosition(levelPos, 75);
                    gi.doEnemyTurn();

                    int runTime = (int)((System.nanoTime() - loopStartTime) / 1000000);
                    try {
                        Thread.sleep(Math.max((int)(MOVEMENT_INTERVAL * 0.9) - runTime, 0));
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

    void freeze() {gi.setPlayerTurn(false);}

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
}
