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
import Game.Registries.ItemRegistry;
import Game.Registries.TagRegistry;
import Game.Tags.OnFireTag;

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

    Player(ViewWindow window, LayerManager lm, GameInstance gameInstance){

        super.lm = lm;

        window.addKeyListener(this);

        Layer playerLayer = new Layer(new SpecialText[1][1], "player", 0, 0, LayerImportances.ENTITY);
        playerLayer.editLayer(0, 0, new SpecialText('@'));

        lm.addLayer(playerLayer);

        setSprite(playerLayer);

        gi = gameInstance;

        setLocation(new Coordinate(0, 0));

        inv = new PlayerInventory(lm, this);

        setMaxHealth(20);
        setStrength(1);

        hud = new HUD(lm, this);

        setName("Player");

        TagRegistry tagRegistry = new TagRegistry();
        addTag(tagRegistry.getTag(TagRegistry.FLAMMABLE), this);

        initSwwoshLayer();

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
                terminatePathing = true;
                FileIO io = new FileIO();
                String path = io.getRootFilePath() + wz.getRoomFilePath();
                Coordinate wzNewPos = new Coordinate(wz.getNewRoomStartX(), wz.getNewRoomStartY());
                DebugWindow.reportf(DebugWindow.GAME, "[Player.checkForWarpZones] Attempting level file path: %1$s \n* wz pos: %2$s\n", path, wzNewPos);
                gi.enterLevel(path, wzNewPos.add(getLocation()).subtract(new Coordinate(wz.getXpos(), wz.getYpos())));
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

    public PlayerInventory getInv() { return inv; }

    public void assignMouseInput(GameMouseInput mi){
        mi.addInputReceiver(inv);
        mi.addInputReceiver(hud);
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
        if (!isFrozen()) {
            if (e.getKeyCode() == KeyEvent.VK_E){
                toggleInventory();
            } else if (e.getKeyCode() == KeyEvent.VK_SHIFT){
                spellMode = true;
                updateHUD();
            } else if (e.getKeyCode() == KeyEvent.VK_L) {
                DebugWindow.reportf(DebugWindow.GAME, "[Player LOG] pos: %1$s\n", getLocation());
                lm.printLayerStack();
            } else if (e.getKeyCode() == KeyEvent.VK_Q){
                inv.openOtherInventory(gi.getEntityAt(mouseLevelPos));
            } else {
                if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) move(1,  0);
                if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A)  move(-1, 0);
                if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S)  move(0,  1);
                if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W)    move(0, -1);
                gi.doEnemyTurn();
            }
            terminatePathing = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SHIFT){
            spellMode = false;
            updateHUD();
        }
    }

    public boolean isInSpellMode() { return spellMode; }

    @Override
    public void onMouseMove(Coordinate levelPos, Coordinate screenPos) {
        mouseLevelPos = levelPos;
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
                Projectile spellProj = new Projectile(this, levelPos, new SpecialText('*'), lm);
                Thread spellThread = new Thread(() -> spellProj.launchProjectile(15, getGameInstance()));
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
