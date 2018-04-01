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

    private int x;
    private int y;

    private int cameraOffsetX = -30;
    private int cameraOffsetY = -15;

    private Layer playerLayer;

    private PlayerInventory inv;

    Player(ViewWindow window, LayerManager lm, GameInstance gameInstance){

        super.lm = lm;

        window.addKeyListener(this);

        lm.setCameraPos(x + cameraOffsetX, y + cameraOffsetY);

        playerLayer = new Layer(new SpecialText[1][1], "player", x, y, 5);
        playerLayer.editLayer(0, 0, new SpecialText('@'));

        lm.addLayer(playerLayer);

        gi = gameInstance;

        inv = new PlayerInventory(lm, this);
        ItemRegistry registry = new ItemRegistry();

        inv.addItem(registry.generateItem(1));
        inv.addItem(registry.generateItem(2));

        setMaxHealth(20);
    }

    public void assignMouseInput(GameMouseInput mi){
        mi.addInputReceiver(inv);
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
            } else {
                if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) x++;
                if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) x--;
                if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S) y++;
                if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) y--;
                playerLayer.setPos(x, y);
                lm.setCameraPos(x + cameraOffsetX, y + cameraOffsetY);
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
            Thread attackThread = new Thread(() -> ((CombatEntity)e).receiveDamage(1));
            attackThread.start();
            return true;
        }
        return false;
    }
}
