package Game;

import Data.ItemStruct;
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

/**
 * Created by Jared on 3/27/2018.
 */
public class Player extends CombatEntity implements MouseInputReceiver, KeyListener{

    private HUD hud;
    private PlayerInventory inv;

    private boolean spellMode = false;

    Player(ViewWindow window, LayerManager lm, GameInstance gameInstance){

        super.lm = lm;

        window.addKeyListener(this);

        setLocation(new Coordinate(40, 43));

        Layer playerLayer = new Layer(new SpecialText[1][1], "player", getLocation().getX(), getLocation().getY(), 5);
        playerLayer.editLayer(0, 0, new SpecialText('@'));

        lm.addLayer(playerLayer);

        setSprite(playerLayer);

        gi = gameInstance;

        updateCameraPos();

        inv = new PlayerInventory(lm, this);
        ItemRegistry registry = new ItemRegistry();

        inv.addItem(registry.generateItem(1).setQty(25));
        inv.addItem(registry.generateItem(2).setQty(15));
        inv.addItem(registry.generateItem(3).setQty(15));
        inv.addItem(registry.generateItem(4).setQty(15));

        setMaxHealth(20);

        hud = new HUD(lm, this);

        setName("Player");

        TagRegistry tagRegistry = new TagRegistry();
        addTag(tagRegistry.getTag(TagRegistry.FLAMMABLE), this);
    }

    private void updateCameraPos(){
        int cameraOffsetX = (lm.getWindow().RESOLUTION_WIDTH / -2) - 1;
        int cameraOffsetY = (lm.getWindow().RESOLUTION_HEIGHT / -2);
        int camNewX = getLocation().getX() + cameraOffsetX;
        int camNewY = getLocation().getY() + cameraOffsetY;
        if (gi != null)
            lm.setCameraPos(Math.max(Math.min(camNewX, gi.getBackdrop().getCols() - lm.getWindow().RESOLUTION_WIDTH), 0), Math.max(Math.min(camNewY, gi.getBackdrop().getRows() - lm.getWindow().RESOLUTION_HEIGHT), 0));
        else
            lm.setCameraPos(camNewX, camNewY);
    }

    public void updateHUD() {hud.updateHUD();}

    @Override
    public void heal(int amount) {
        super.heal(amount);
        hud.updateHUD();
    }

    @Override
    public void receiveDamage(int amount) {
        super.receiveDamage(amount);
        hud.updateHUD();
    }

    @Override
    protected void doAttackEvent(CombatEntity ce) {
        if (getWeapon() != null && getWeapon().getItemData().getQty() == 1)
            inv.removeItem(getWeapon());
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

    @Override
    public void keyPressed(KeyEvent e) {
        if (!isFrozen()) {
            if (e.getKeyCode() == KeyEvent.VK_E){
                if (inv.isShowing()) inv.close();
                else inv.show();
                hud.updateHUD();
            } else if (e.getKeyCode() == KeyEvent.VK_SHIFT){
                spellMode = true;
                updateHUD();
            } else if (e.getKeyCode() == KeyEvent.VK_L) {
                lm.printLayerStack();
            } else {
                if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) move(1,  0);
                if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A)  move(-1, 0);
                if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S)  move(0,  1);
                if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W)    move(0, -1);
                updateCameraPos();
                gi.doEnemyTurn();
            }
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

    }

    @Override
    public boolean onMouseClick(Coordinate levelPos, Coordinate screenPos) {
        if (!isFrozen()) {
            if (!spellMode) {
                Thread attackThread = new Thread(() -> {
                    freeze();
                    doWeaponAttack(levelPos);
                    gi.doEnemyTurn();
                });
                attackThread.start();
            } else {
                System.out.printf("[Player.onMouseClick] Spell casted! %1$s\n", levelPos);
                OnFireTag forConvenience = new OnFireTag();
                forConvenience.attemptFireTileSpread(getGameInstance().getCurrentLevel(), levelPos, 1);
            }
        }
        return false;
    }

    public void doEnemyTurn(){
        gi.doEnemyTurn();
    }

    void freeze() {gi.setPlayerTurn(false);}

    void unfreeze() {gi.setPlayerTurn(true);}

    boolean isFrozen() {return !gi.isPlayerTurn(); }
}
