package Game;

import Data.Coordinate;
import Data.FileIO;
import Data.LayerImportances;
import Data.WarpZone;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Engine.ViewWindow;
import Game.Entities.CombatEntity;
import Game.Entities.Entity;
import Game.Registries.TagRegistry;
import Game.Spells.FireBoltSpell;
import Game.Spells.IceBoltSpell;
import Game.Spells.MagicBoltSpell;
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

    private HUD hud;
    private PlayerInventory inv;

    private Thread pathingThread;
    private boolean terminatePathing;

    private Coordinate mouseLevelPos;

    private boolean spellMode = false;

    private ArrayList<Spell> spells = new ArrayList<>();
    private Spell equippedSpell;
    private int numberSpellBeads = 2;
    private ArrayList<Integer> cooldowns = new ArrayList<>();

    private int noEnterWarpZoneTimer = 0;

    private Coordinate movementVector = new Coordinate(0, 0);
    private final Coordinate NORTH    = new Coordinate(0, -1);
    private final Coordinate EAST     = new Coordinate(1, 0);
    private final Coordinate SOUTH    = new Coordinate(0, 1);
    private final Coordinate WEST     = new Coordinate(-1, 0);
    private Thread movementThread;
    private final int MOVEMENT_INTERVAL = 125;
    
    private ArrayList<Integer> downKeyCodes = new ArrayList<>(); //KeyCodes of keys currently pressed down on the keyboard

    Player(ViewWindow window, LayerManager lm, GameInstance gameInstance){

        super.lm = lm;

        window.addKeyListener(this);

        Layer playerLayer = new Layer(new SpecialText[1][1], "player", 0, 0, LayerImportances.ENTITY);
        playerLayer.editLayer(0, 0, new SpecialText('@', new Color(224, 255, 217)));

        lm.addLayer(playerLayer);

        setSprite(playerLayer);

        gi = gameInstance;

        setLocation(new Coordinate(0, 0));

        inv = new PlayerInventory(lm, this);

        setMaxHealth(20);
        setStrength(1);

        setName("Player");

        addTag(TagRegistry.FLAMMABLE, this);

        initSwwoshLayer();

        MagicBoltSpell spell = new MagicBoltSpell();
        spells.add(spell);
        spells.add(new FireBoltSpell());
        spells.add(new IceBoltSpell());
        equippedSpell = spell;

        hud = new HUD(lm, this);
    }

    public void updateCameraPos(){
        int cameraOffsetX = (lm.getWindow().RESOLUTION_WIDTH / -2) - 1;
        int cameraOffsetY = (lm.getWindow().RESOLUTION_HEIGHT / -2);
        int camNewX = getLocation().getX() + cameraOffsetX;
        int camNewY = getLocation().getY() + cameraOffsetY;
        if (gi != null)
            lm.setCameraPos(Math.max(Math.min(camNewX, gi.getBackdrop().getCols() - lm.getWindow().RESOLUTION_WIDTH), 0), Math.max(Math.min(camNewY, gi.getBackdrop().getRows() - lm.getWindow().RESOLUTION_HEIGHT), 0));
        else
            lm.setCameraPos(camNewX, camNewY);
        getSprite().setPos(getLocation());
    }

    public void updateHUD() {hud.updateHUD();}

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
                    DebugWindow.reportf(DebugWindow.GAME, "[Player.checkForWarpZones] Attempting level file path: %1$s \n* wz pos: %2$s\n", path, wzNewPos);
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
        DebugWindow.reportf(DebugWindow.GAME, "[Player.onTurn] noEnterWarpZoneTimer: %1$d", noEnterWarpZoneTimer);
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

    public void assignMouseInput(GameMouseInput mi){
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
        DebugWindow.reportf(DebugWindow.STAGE, "[Player] movementVector: %1$s", movementVector);
    }

    //Holding down a key on the keyboard fires keyPressed() a gajillion times. This method is ONLY ran when a key is pressed down.
    private void onKeyDown(int keyCode){
        if (!isFrozen()) {
            if (keyCode == KeyEvent.VK_E){
                toggleInventory();
            } else if (keyCode == KeyEvent.VK_SHIFT){
                spellMode = true;
                updateHUD();
            } else if (keyCode == KeyEvent.VK_L) {
                DebugWindow.reportf(DebugWindow.GAME, "[Player LOG] pos: %1$s\n", getLocation());
                lm.printLayerStack();
            } else if (keyCode == KeyEvent.VK_Q){
                inv.openOtherInventory(gi.getEntityAt(mouseLevelPos));
            } else {
                if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) movementKeyDown(EAST);
                if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A)  movementKeyDown(WEST);
                if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S)  movementKeyDown(SOUTH);
                if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W)    movementKeyDown(NORTH);
            }
            terminatePathing = true;
        }
        DebugWindow.reportf(DebugWindow.STAGE, "[Player] movementVector: %1$s", movementVector);
    }

    private void movementKeyDown(Coordinate vector){
        movementVector = movementVector.add(vector);
        movementVector = new Coordinate(Math.max(-1, Math.min(1, movementVector.getX())), Math.max(-1, Math.min(1, movementVector.getY()))); //Sanitates the movement vector
        if (movementThread == null || !movementThread.isAlive()) {
            movementThread = new Thread(() -> {
                while (!movementVector.equals(new Coordinate(0, 0))){
                    /**/
                    if (!isFrozen()) {
                        if (movementVector.getX() != 0) {
                            move(movementVector.getX(), 0);
                            try {
                                gi.doEnemyTurn().join();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        if (movementVector.getY() != 0) {
                            move(0, movementVector.getY());
                            try {
                                gi.doEnemyTurn().join();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    /**/
                    }
                    sleepMoveThread(MOVEMENT_INTERVAL);
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
            DebugWindow.reportf(DebugWindow.STAGE, e.getMessage());
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
            DebugWindow.reportf(DebugWindow.STAGE, "[Player.onMouseClick] Left click event! l:%1$s s:%2$s", levelPos, screenPos);
            doLeftClick(levelPos);
        } else if (mouseButton == MouseEvent.BUTTON3){
            DebugWindow.reportf(DebugWindow.STAGE, "[Player.onMouseClick] Right click event! l:%1$s s:%2$s", levelPos, screenPos);
            doRightClick(levelPos);
        }
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
                DebugWindow.reportf(DebugWindow.GAME, "[Player.onMouseClick] Spell casted! %1$s", levelPos);
                Thread spellThread = new Thread(() -> {
                    if (isSpellReady()) {
                        freeze();
                        cooldowns.add(equippedSpell.castSpell(levelPos, this, getGameInstance()));
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
                Entity target = gi.getEntityAt(levelPos);
                while ((prevPos == null || !prevPos.equals(getLocation())) && !terminatePathing) {
                    prevPos = new Coordinate(getLocation().getX(), getLocation().getY());
                    pathToPosition(levelPos, 75);
                    gi.doEnemyTurn();
                    if (target != null && getLocation().stepDistance(target.getLocation()) <= 1){
                        target.onInteract(this);
                    }
                    try {
                        Thread.sleep(100);
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

    void unfreeze() {gi.setPlayerTurn(true);}

    boolean isFrozen() {return !gi.isPlayerTurn(); }
}
