package Game;

import Data.ItemStruct;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Engine.ViewWindow;
import Game.Entities.CombatEntity;
import Game.Entities.Entity;
import Game.Registries.ItemRegistry;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Created by Jared on 3/27/2018.
 */
public class Player extends CombatEntity implements MouseInputReceiver, KeyListener{
    private final int cameraOffsetX = -30;
    private final int cameraOffsetY = -15;

    private HUD hud;
    private PlayerInventory inv;

    Player(ViewWindow window, LayerManager lm, GameInstance gameInstance){

        super.lm = lm;

        window.addKeyListener(this);

        setLocation(new Coordinate(122, 58));

        updateCameraPos();

        Layer playerLayer = new Layer(new SpecialText[1][1], "player", getLocation().getX(), getLocation().getY(), 5);
        playerLayer.editLayer(0, 0, new SpecialText('@'));

        lm.addLayer(playerLayer);

        setSprite(playerLayer);

        gi = gameInstance;

        inv = new PlayerInventory(lm, this);
        ItemRegistry registry = new ItemRegistry();

        inv.addItem(registry.generateItem(1).setQty(25));
        inv.addItem(registry.generateItem(2));
        inv.addItem(registry.generateItem(3).setQty(25));

        setMaxHealth(20);
        health = 1;

        hud = new HUD(lm, this);
    }

    private void updateCameraPos(){
        lm.setCameraPos(getLocation().getX() + cameraOffsetX, getLocation().getY() + cameraOffsetY);
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
        if (gi.isPlayerTurn()) {
            if (e.getKeyCode() == KeyEvent.VK_E){
                if (inv.isShowing()) inv.close();
                else inv.show();
                hud.updateHUD();
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

    }

    @Override
    public void onMouseMove(Coordinate levelPos, Coordinate screenPos) {

    }

    @Override
    public boolean onMouseClick(Coordinate levelPos, Coordinate screenPos) {
        System.out.println("[Player] Attack!");
        Entity e = getGameInstance().getEntityAt(levelPos);
        if (e != null && e instanceof CombatEntity){
            Thread attackThread = new Thread(() -> {
                e.receiveDamage(1);
                gi.doEnemyTurn();
            });
            attackThread.start();
            return true;
        }
        return false;
    }

    void doEnemyTurn(){
        gi.doEnemyTurn();
    }
}
